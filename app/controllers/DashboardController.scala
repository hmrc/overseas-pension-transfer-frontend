/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import config.FrontendAppConfig
import controllers.actions.{IdentifierAction, SchemeDataAction}
import models.audit.JourneyStartedType.ContinueTransfer
import models.authentication.{PsaUser, PspUser}
import models.{AllTransfersItem, DashboardData, PensionSchemeDetails, QtNumber, QtStatus, TransferId, TransferNumber, TransferReportQueryParams, TransferSearch}
import pages.DashboardPage
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._
import queries.PensionSchemeDetailsQuery
import queries.dashboard.TransfersOverviewQuery
import repositories.{DashboardSessionRepository, SessionRepository}
import services.{LockService, TransferService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.{PaginatedAllTransfersViewModel, SearchBarViewModel}
import views.html.DashboardView
import views.html.components.AppBreadcrumbs

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DashboardController @Inject() (
    val controllerComponents: MessagesControllerComponents,
    repo: DashboardSessionRepository,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    schemeData: SchemeDataAction,
    transferService: TransferService,
    view: DashboardView,
    userAnswersService: UserAnswersService,
    lockService: LockService,
    appBreadcrumbs: AppBreadcrumbs
  )(implicit ec: ExecutionContext,
    appConfig: FrontendAppConfig
  ) extends FrontendBaseController with I18nSupport with Logging {

  private val lockTtlSeconds: Long = appConfig.dashboardLockTtl

  def onPageLoad(page: Int, search: Option[String]): Action[AnyContent] = identify.async { implicit request =>
    val id          = request.authenticatedUser.internalId
    val lockWarning = request.flash.get("lockWarning") // flash for warning

    sessionRepository.clear(id) flatMap { _ =>
      repo.get(id).flatMap {
        case None =>
          logger.warn(s"[DashboardController][onPageLoad] No dashboard data found this customer")
          Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))

        case Some(dashboardData) =>
          dashboardData.get(PensionSchemeDetailsQuery).fold {
            logger.warn(s"[DashboardController][onPageLoad] Missing PensionSchemeDetails for this customer")
            Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
          } { pensionSchemeDetails =>
            dashboardData.get(TransfersOverviewQuery) match {
              case None            =>
                renderDashboard(page, search, dashboardData, pensionSchemeDetails, lockWarning, request.authenticatedUser)
              case Some(transfers) =>
                transfers.map {
                  val owner =
                    request.authenticatedUser match {
                      case PsaUser(psaId, _, _) => psaId.value
                      case PspUser(pspId, _, _) => pspId.value
                    }

                  transfer =>
                    transfer.transferId match {
                      case TransferNumber(transferRef) =>
                        logger.info(s"[DashboardController][onPageLoad] lock released for $transferRef")
                        lockService.releaseLock(transferRef, owner)
                      case QtNumber(qtRefefence)       =>
                        logger.info(s"[DashboardController][onPageLoad] lock released for $qtRefefence")
                        lockService.releaseLock(qtRefefence, owner)
                    }
                }
                renderDashboard(page, search, dashboardData, pensionSchemeDetails, lockWarning, request.authenticatedUser)
            }
          }
      }
    }
  }

  def onTransferClick(): Action[AnyContent] = (identify andThen schemeData).async { implicit request =>
    val params     = TransferReportQueryParams.fromRequest(request)
    val owner      = request.authenticatedUser match {
      case PsaUser(psaId, _, _) => psaId.value
      case PspUser(pspId, _, _) => pspId.value
    }
    val transferId = params.transferId.getOrElse(TransferId("-"))
    val pstr       = params.pstr.getOrElse {
      throw new IllegalStateException("[DashboardController][onTransferClick] Missing PSTR in query params")
    }

    if (params.qtStatus.contains(QtStatus.InProgress)) {
      for {
        userAnswersResult <- userAnswersService.getExternalUserAnswers(transferId, pstr, QtStatus.InProgress, None)
        allTransfersItem   = userAnswersResult.toOption.map(userAnswersService.toAllTransfersItem)
        lockAcquired      <- lockService.takeLockWithAudit(
                               transferId,
                               owner,
                               lockTtlSeconds,
                               request.authenticatedUser,
                               request.schemeDetails,
                               ContinueTransfer,
                               allTransfersItem
                             )
        result            <- if (lockAcquired) {
                               val dashboardData  = DashboardData.empty
                               val redirectTarget = DashboardPage.nextPage(dashboardData, params.qtStatus, Some(params))
                               Future.successful(Redirect(redirectTarget))
                             } else {
                               Future.successful(
                                 Redirect(routes.DashboardController.onPageLoad(params.currentPage))
                                   .flashing("lockWarning" -> params.memberName)
                               )
                             }
      } yield result
    } else {
      val dashboardData  = DashboardData.empty
      val redirectTarget = DashboardPage.nextPage(dashboardData, params.qtStatus, Some(params))
      Future.successful(Redirect(redirectTarget))
    }
  }

  private def renderDashboard(
      page: Int,
      search: Option[String],
      dashboardData: DashboardData,
      pensionSchemeDetails: PensionSchemeDetails,
      lockWarning: Option[String],
      authenticatedUser: models.authentication.AuthenticatedUser
    )(implicit request: Request[_],
      appConfig: FrontendAppConfig
    ): Future[Result] = {

    transferService.getAllTransfersData(dashboardData, pensionSchemeDetails.pstrNumber).flatMap {
      _.fold(
        err => {
          logger.warn(s"[DashboardController] getAllTransfersData failed: $err")
          Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        },
        updatedData => {

          val allTransfers      = updatedData.get(TransfersOverviewQuery).getOrElse(Seq.empty)
          val expiringItems     = repo.findExpiringWithin2Days(allTransfers)
          val filteredTransfers = getFilteredTransfers(allTransfers, search)
          val transfersVm       = buildTransfersVm(filteredTransfers, allTransfers.size, page, search, lockWarning)
          val searchBarVm       = buildSearchBarVm(search)
          val mpsLink           = appConfig.mpsHomeUrl
          val pensionSchemeLink = appConfig.getPensionSchemeUrl(
            srn       = pensionSchemeDetails.srnNumber.value,
            isPspUser = authenticatedUser.isInstanceOf[models.authentication.PspUser]
          )

          repo.set(updatedData).map { _ =>
            Ok(
              view(
                pensionSchemeDetails.schemeName,
                DashboardPage.nextPage(updatedData, None, None).url,
                transfersVm,
                searchBarVm,
                expiringItems,
                mpsLink,
                isSearch          = search.exists(_.trim.nonEmpty),
                breadcrumbs       = appBreadcrumbs(mpsLink, pensionSchemeLink),
                pensionSchemeLink = pensionSchemeLink
              )
            )
          }
        }
      )
    }
  }

  private def buildTransfersVm(
      items: Seq[AllTransfersItem],
      totalItems: Int,
      page: Int,
      search: Option[String],
      lockWarning: Option[String]
    )(implicit messages: Messages,
      appConfig: FrontendAppConfig
    ): PaginatedAllTransfersViewModel =
    PaginatedAllTransfersViewModel.build(
      items       = items,
      page        = page,
      pageSize    = appConfig.transfersPerPage,
      urlForPage  = pageUrl(search),
      lockWarning = lockWarning,
      totalItems  = Some(totalItems)
    )

  private def buildSearchBarVm(
      search: Option[String]
    )(implicit messages: Messages
    ): SearchBarViewModel = {

    val clearUrl: Option[String] =
      search.map(_ => routes.DashboardController.onPageLoad(page = 1, search = None).url)

    SearchBarViewModel(
      label    = messages("dashboard.search.heading"),
      action   = routes.DashboardController.onPageLoad().url,
      value    = search.map(_.trim).filter(_.nonEmpty),
      hint     = Some(messages("dashboard.search.hintText")),
      clearUrl = clearUrl
    )
  }

  private def getFilteredTransfers(
      all: Seq[AllTransfersItem],
      search: Option[String]
    ): Seq[AllTransfersItem] =
    search.filter(_.trim.nonEmpty) match {
      case Some(term) => TransferSearch.filterTransfers(all, term)
      case None       => all
    }

  private def pageUrl(search: Option[String])(p: Int): String =
    routes.DashboardController.onPageLoad(p, search).url

}

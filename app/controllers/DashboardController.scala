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
import models.{AllTransfersItem, DashboardData, PensionSchemeDetails, QtNumber, QtStatus, TransferId, TransferNumber, TransferReportQueryParams}
import pages.DashboardPage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._
import queries.PensionSchemeDetailsQuery
import queries.dashboard.TransfersOverviewQuery
import repositories.{DashboardSessionRepository, SessionRepository}
import services.{LockService, TransferService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.{PaginatedAllTransfersViewModel, SearchBarViewModel}
import views.html.DashboardView

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
    appConfig: FrontendAppConfig,
    userAnswersService: UserAnswersService,
    lockService: LockService
  )(implicit ec: ExecutionContext
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
                renderDashboard(page, search, dashboardData, pensionSchemeDetails, appConfig, lockWarning)
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
                renderDashboard(page, search, dashboardData, pensionSchemeDetails, appConfig, lockWarning)
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
      appConfig: FrontendAppConfig,
      lockWarning: Option[String]
    )(implicit request: Request[_]
    ): Future[Result] = {
    transferService.getAllTransfersData(dashboardData, pensionSchemeDetails.pstrNumber).flatMap {
      _.fold(
        err => {
          logger.warn(s"[DashboardController] getAllTransfersData failed: $err")
          Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        },
        updatedData => {
          val allTransfers: Seq[AllTransfersItem] = updatedData.get(TransfersOverviewQuery).getOrElse(Seq.empty)
          val expiringItems                       = repo.findExpiringWithin2Days(allTransfers)
          val filteredTransfers                   =
            search.filter(_.trim.nonEmpty) match {
              case Some(term) => filterTransfers(allTransfers, term)
              case None       => allTransfers
            }
          val allTransfersViewModel               = PaginatedAllTransfersViewModel.build(
            items       = filteredTransfers,
            page        = page,
            pageSize    = appConfig.transfersPerPage,
            urlForPage  = pageUrl(search),
            lockWarning = lockWarning
          )
          val searchBarViewModel                  = if (appConfig.allowDashboardSearch) {
            Some(SearchBarViewModel(
              action = routes.DashboardController.onPageLoad().url,
              value  = search.map(_.trim).filter(_.nonEmpty)
            ))
          } else { None }
          val srn                                 = pensionSchemeDetails.srnNumber.value
          val mpsLink                             = s"${appConfig.mpsBaseUrl}$srn"
          val isSearch: Boolean                   =
            search.exists(_.trim.nonEmpty)
          repo.set(updatedData).map { _ =>
            Ok(view(
              pensionSchemeDetails.schemeName,
              DashboardPage.nextPage(updatedData, None, None).url,
              allTransfersViewModel,
              searchBarViewModel,
              expiringItems,
              mpsLink,
              isSearch
            ))
          }
        }
      )
    }
  }

  private def pageUrl(search: Option[String])(p: Int): String =
    routes.DashboardController.onPageLoad(p, search).url

  private def filterTransfers(
      transfers: Seq[AllTransfersItem],
      rawTerm: String
    ): Seq[AllTransfersItem] = {

    val term = rawTerm.trim.toLowerCase

    if (term.isEmpty) {
      transfers
    } else {
      transfers.filter { t =>
        val name = s"${t.memberFirstName.getOrElse("")} ${t.memberSurname.getOrElse("")}".toLowerCase
        val nino = t.nino.map(_.toLowerCase).getOrElse("")
        val ref  = t.transferId.value.toLowerCase
        name.contains(term) || nino.contains(term) || ref.contains(term)
      }
    }
  }
}

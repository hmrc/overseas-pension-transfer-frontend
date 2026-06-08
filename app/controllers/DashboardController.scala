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

import models.authentication.PsaUser
import models.authentication.PspUser
import services.LockService
import services.TransferService
import services.UserAnswersService
import queries.PensionSchemeDetailsQuery
import play.api.mvc.*
import pages.DashboardPage
import views.html.DashboardView
import controllers.actions.IdentifierAction
import controllers.actions.SchemeDataAction
import views.html.components.AppBreadcrumbs
import play.api.Logging
import repositories.DashboardSessionRepository
import repositories.SessionRepository
import models.requests.SchemeRequest
import queries.dashboard.TransfersOverviewQuery
import config.FrontendAppConfig
import models._
import models.audit.JourneyStartedType.ContinueTransfer
import play.api.i18n.I18nSupport
import play.api.i18n.Messages
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.PaginatedAllTransfersViewModel
import viewmodels.SearchBarViewModel

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import java.time.Clock
import java.time.Instant
import javax.inject.*

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
  appBreadcrumbs: AppBreadcrumbs,
  clock: Clock
)(implicit ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val lockTtlSeconds: Long = appConfig.dashboardLockTtl

  def onPageLoad(page: Int, search: Option[String]): Action[AnyContent] = (identify andThen schemeData).async {
    implicit request =>
      val id          = request.authenticatedUser.internalId
      val lockWarning = request.flash.get("lockWarning") // flash for warning
      userAnswersService.clearEmptyUserAnswers(id).flatMap { _ =>
        sessionRepository.clear(id) flatMap { _ =>
          repo.get(id).flatMap {
            case None =>
              logger.warn(s"[DashboardController][onPageLoad] No dashboard data found for this customer")
              Future.successful(Redirect(DashboardPage.nextPageRecovery()))

            case Some(dashboardData) =>
              dashboardData
                .get(PensionSchemeDetailsQuery)
                .fold {
                  logger.warn(s"[DashboardController][onPageLoad] Missing PensionSchemeDetails for this customer")
                  Future.successful(Redirect(DashboardPage.nextPageRecovery()))
                } { pensionSchemeDetails =>
                  dashboardData.get(TransfersOverviewQuery) match {
                    case None            =>
                      renderDashboard(
                        page,
                        search,
                        dashboardData,
                        pensionSchemeDetails,
                        lockWarning,
                        request.authenticatedUser
                      )
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
                      renderDashboard(
                        page,
                        search,
                        dashboardData,
                        pensionSchemeDetails,
                        lockWarning,
                        request.authenticatedUser
                      )
                  }
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
        userAnswersResult <-
          userAnswersService
            .getExternalUserAnswers(transferId, pstr, QtStatus.InProgress, None, request.schemeDetails.srnNumber)
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
                               val dashboardData  = DashboardData.empty(Instant.now(clock))
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
      val dashboardData  = DashboardData.empty(Instant.now(clock))
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
  )(implicit request: SchemeRequest[_], appConfig: FrontendAppConfig): Future[Result] =

    transferService
      .getAllTransfersData(dashboardData, pensionSchemeDetails.pstrNumber, pensionSchemeDetails.srnNumber)
      .flatMap {
        _.fold(
          err => {
            logger.warn(s"[DashboardController] getAllTransfersData failed: $err")
            Future.successful(Redirect(DashboardPage.nextPageRecovery()))
          },
          updatedData => {

            val allTransfers      = updatedData.get(TransfersOverviewQuery).getOrElse(Seq.empty)
            val filteredTransfers = getFilteredTransfers(allTransfers, search)
            val transfersVm       = buildTransfersVm(filteredTransfers, allTransfers.size, page, search, lockWarning)
            val searchBarVm       = buildSearchBarVm(search)
            val mpsLink           = appConfig.mpsHomeUrl
            val pensionSchemeLink = routes.DashboardController
              .clearAndExit(
                appConfig.getPensionSchemeUrl(
                  srn = pensionSchemeDetails.srnNumber.value,
                  isPspUser = authenticatedUser.isInstanceOf[models.authentication.PspUser]
                )
              )
              .url

            repo.set(updatedData).map { _ =>
              Ok(
                view(
                  pensionSchemeDetails.schemeName,
                  DashboardPage.nextPage(updatedData, None, None).url,
                  transfersVm,
                  searchBarVm,
                  isSearch = search.exists(_.trim.nonEmpty),
                  breadcrumbs = appBreadcrumbs(mpsLink, pensionSchemeLink),
                  pensionSchemeLink = pensionSchemeLink
                )
              )
            }
          }
        )
      }

  private def buildTransfersVm(
    items: Seq[AllTransfersItem],
    totalItems: Int,
    page: Int,
    search: Option[String],
    lockWarning: Option[String]
  )(implicit messages: Messages, appConfig: FrontendAppConfig): PaginatedAllTransfersViewModel =
    PaginatedAllTransfersViewModel.build(
      items = items,
      page = page,
      pageSize = appConfig.transfersPerPage,
      urlForPage = pageUrl(search),
      lockWarning = lockWarning,
      totalItems = Some(totalItems)
    )

  private def buildSearchBarVm(
    search: Option[String]
  )(implicit messages: Messages): SearchBarViewModel = {

    val clearUrl: Option[String] =
      search.map(_ => routes.DashboardController.onPageLoad(search = None).url)

    SearchBarViewModel(
      label = messages("dashboard.search.heading"),
      action = routes.DashboardController.onPageLoad().url,
      value = search.map(_.trim).filter(_.nonEmpty),
      hint = Some(messages("dashboard.search.hintText")),
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

  def clearAndExit(redirect: String): Action[AnyContent] = identify.async { implicit request =>
    val id = request.authenticatedUser.internalId
    for {
      _ <- repo.clear(id)
      _ <- sessionRepository.clear(id)
    } yield Redirect(redirect)
  }

  private def pageUrl(search: Option[String])(p: Int): String =
    routes.DashboardController.onPageLoad(p, search).url
}

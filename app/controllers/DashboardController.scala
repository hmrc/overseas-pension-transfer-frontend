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
import models.{DashboardData, PensionSchemeDetails, QtNumber, QtStatus, TransferId, TransferNumber, TransferReportQueryParams}
import pages.DashboardPage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._
import queries.PensionSchemeDetailsQuery
import queries.dashboard.TransfersOverviewQuery
import repositories.{DashboardSessionRepository, SessionRepository}
import services.{LockService, TransferService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.PaginatedAllTransfersViewModel
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
    appConfig: FrontendAppConfig,
    userAnswersService: UserAnswersService,
    lockService: LockService,
    appBreadcrumbs: AppBreadcrumbs
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with Logging {

  private val lockTtlSeconds: Long = appConfig.dashboardLockTtl

  def onPageLoad(page: Int): Action[AnyContent] = identify.async { implicit request =>
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
                renderDashboard(page, dashboardData, pensionSchemeDetails, lockWarning)
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

                renderDashboard(page, dashboardData, pensionSchemeDetails, lockWarning)
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
      dashboardData: DashboardData,
      pensionSchemeDetails: PensionSchemeDetails,
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
          val allTransfers  = updatedData.get(TransfersOverviewQuery).getOrElse(Seq.empty)
          val expiringItems = repo.findExpiringWithin2Days(allTransfers)

          val viewModel = PaginatedAllTransfersViewModel.build(
            items       = allTransfers,
            page        = page,
            pageSize    = appConfig.transfersPerPage,
            urlForPage  = pageUrl,
            lockWarning = lockWarning
          )

          val srn               = pensionSchemeDetails.srnNumber.value
          val mpsLink           = appConfig.mpsHomeUrl
          val pensionSchemeLink = s"${appConfig.pensionSchemeSummaryUrl}$srn"

          repo.set(updatedData).map { _ =>
            Ok(
              view(
                schemeName        = pensionSchemeDetails.schemeName,
                nextPage          = DashboardPage.nextPage(updatedData, None, None).url,
                vm                = viewModel,
                expiringItems     = expiringItems,
                pensionSchemeLink = pensionSchemeLink,
                breadcrumbs       = appBreadcrumbs(mpsLink, pensionSchemeLink)
              )
            )
          }
        }
      )
    }
  }

  private def pageUrl(n: Int): String =
    routes.DashboardController.onPageLoad(n).url
}

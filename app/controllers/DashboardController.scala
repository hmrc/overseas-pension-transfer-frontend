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
import controllers.actions.IdentifierAction
import models.authentication.{Psa, PsaUser, Psp, PspUser}
import models.{DashboardData, PensionSchemeDetails, TransferReportQueryParams}
import pages.DashboardPage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._
import queries.PensionSchemeDetailsQuery
import queries.dashboard.TransfersOverviewQuery
import repositories.{DashboardSessionRepository, SessionRepository}
import services.TransferService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.lock.LockRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import viewmodels.PaginatedAllTransfersViewModel
import views.html.DashboardView

import javax.inject._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DashboardController @Inject() (
    val controllerComponents: MessagesControllerComponents,
    repo: DashboardSessionRepository,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    transferService: TransferService,
    view: DashboardView,
    appConfig: FrontendAppConfig,
    lockRepository: LockRepository
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with Logging {

  private val lockTtlSeconds: Long = appConfig.dashboardLockTtl

  def onPageLoad(page: Int): Action[AnyContent] = identify.async { implicit request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    val id                         = request.authenticatedUser.internalId
    val lockWarning                = request.flash.get("lockWarning") // flash for warning

    sessionRepository.clear(id) flatMap { _ =>
      repo.get(id).flatMap {
        case None =>
          logger.warn(s"[DashboardController][onPageLoad] No dashboard data found for $id")
          Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))

        case Some(dashboardData) =>
          dashboardData.get(PensionSchemeDetailsQuery).fold {
            logger.warn(s"[DashboardController][onPageLoad] Missing PensionSchemeDetails for $id")
            Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
          } { pensionSchemeDetails =>
            dashboardData.get(TransfersOverviewQuery) match {
              case None            =>
                renderDashboard(page, dashboardData, pensionSchemeDetails, lockWarning)
              case Some(transfers) =>
                transfers.map {
                  transfer =>
                    (transfer.transferReference, transfer.qtReference) match {
                      case (Some(transferRef), None)              =>
                        logger.info(s"[DashboardController][onPageLoad] lock released for $transferRef")
                        lockRepository.releaseLock(transferRef, request.authenticatedUser.internalId)
                      case (None, Some(qtRefefence))              =>
                        logger.info(s"[DashboardController][onPageLoad] lock released for $qtRefefence")
                        lockRepository.releaseLock(qtRefefence.value, request.authenticatedUser.internalId)
                      case (Some(transferRef), Some(qtRefefence)) =>
                        logger.info(s"lock released for $transferRef and $qtRefefence")
                        lockRepository.releaseLock(transferRef, request.authenticatedUser.internalId)
                        lockRepository.releaseLock(qtRefefence.value, request.authenticatedUser.internalId)
                      case (None, None)                           => ()
                    }
                }

                renderDashboard(page, dashboardData, pensionSchemeDetails, lockWarning)
            }

          }
      }
    }
  }

  def onTransferClick(): Action[AnyContent] = identify.async { implicit request =>
    val params = TransferReportQueryParams.fromRequest(request)
    val owner  = request.authenticatedUser match {
      case PsaUser(psaId, _, _, _) => psaId.value
      case PspUser(pspId, _, _, _) => pspId.value
    }
    val lockId = params.qtReference.filter(_.nonEmpty)
      .orElse(params.transferReference.filter(_.nonEmpty))
      .getOrElse("-")

    lockRepository.takeLock(lockId, owner, lockTtlSeconds.seconds).flatMap {
      case Some(_) =>
        logger.info(s"[DashboardController][onTransferClick] Lock acquired for $lockId by $owner")
        val dashboardData  = DashboardData.empty
        val redirectTarget = DashboardPage.nextPage(dashboardData, params.qtStatus, Some(params))
        Future.successful(Redirect(redirectTarget))

      case None =>
        logger.info(s"[DashboardController][onTransferClick] Lock already taken for $lockId")
        Future.successful(
          Redirect(routes.DashboardController.onPageLoad(params.currentPage))
            .flashing("lockWarning" -> params.memberName)
        )
    }
  }

  private def renderDashboard(
      page: Int,
      dashboardData: DashboardData,
      pensionSchemeDetails: PensionSchemeDetails,
      lockWarning: Option[String]
    )(implicit request: Request[_],
      hc: HeaderCarrier
    ): Future[Result] = {

    transferService.getAllTransfersData(dashboardData, pensionSchemeDetails.pstrNumber).flatMap {
      _.fold(
        err => {
          logger.warn(s"[DashboardController] getAllTransfersData failed: $err")
          Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        },
        updatedData => {
          val allTransfers  = updatedData.get(TransfersOverviewQuery).getOrElse(Seq.empty)
          val expiringItems = repo.findExpiringWithin7Days(allTransfers)

          val viewModel = PaginatedAllTransfersViewModel.build(
            items       = allTransfers,
            page        = page,
            pageSize    = appConfig.transfersPerPage,
            urlForPage  = pageUrl,
            lockWarning = lockWarning
          )

          repo.set(updatedData).map { _ =>
            Ok(
              view(
                schemeName    = pensionSchemeDetails.schemeName,
                nextPage      = DashboardPage.nextPage(updatedData, None, None).url,
                vm            = viewModel,
                expiringItems = expiringItems
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

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
import models.{DashboardData, PensionSchemeDetails}
import pages.DashboardPage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._
import queries.PensionSchemeDetailsQuery
import queries.dashboard.TransfersOverviewQuery
import repositories.{DashboardSessionRepository, SessionRepository}
import services.TransferService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import viewmodels.PaginatedAllTransfersViewModel
import views.html.DashboardView

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DashboardController @Inject() (
    val controllerComponents: MessagesControllerComponents,
    repo: DashboardSessionRepository,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    transferService: TransferService,
    view: DashboardView,
    appConfig: FrontendAppConfig
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(page: Int): Action[AnyContent] = identify.async { implicit request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    val id                         = request.authenticatedUser.internalId

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
            renderDashboard(page, dashboardData, pensionSchemeDetails)
          }
      }
    }
  }

  private def renderDashboard(
      page: Int,
      dashboardData: DashboardData,
      pensionSchemeDetails: PensionSchemeDetails
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
            items      = allTransfers,
            page       = page,
            pageSize   = appConfig.transfersPerPage,
            urlForPage = pageUrl
          )

          repo.set(updatedData).map { _ =>
            Ok(
              view(
                schemeName    = pensionSchemeDetails.schemeName,
                nextPage      = DashboardPage.nextPage(updatedData).url,
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

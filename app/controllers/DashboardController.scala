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
import pages.DashboardPage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._
import queries.PensionSchemeDetailsQuery
import queries.dashboard.TransfersOverviewQuery
import repositories.DashboardSessionRepository
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
    identify: IdentifierAction,
    transferService: TransferService,
    view: DashboardView,
    appConfig: FrontendAppConfig
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with Logging {

  private def currentPage(implicit req: Request[_]): Int =
    req.getQueryString("page").flatMap(_.toIntOption).filter(_ >= 1).getOrElse(1)

  private def pageUrl(n: Int): String =
    s"${routes.DashboardController.onPageLoad().url}?page=$n"

  def onPageLoad: Action[AnyContent] = identify.async { implicit request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    val id                         = request.authenticatedUser.internalId

    repo.get(id).flatMap {
      case Some(dd) =>
        dd.get(PensionSchemeDetailsQuery) match {
          case Some(psd) =>
            transferService.getAllTransfersData(dd, psd.pstrNumber).flatMap {
              case Left(e)        =>
                logger.warn(s"[DashboardController] getAllTransfersData failed: $e")
                Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
              case Right(updated) =>
                val items = updated.get(TransfersOverviewQuery).getOrElse(Seq.empty)

                val vm = PaginatedAllTransfersViewModel.build(
                  items      = items,
                  page       = currentPage,
                  pageSize   = appConfig.transfersPerPage,
                  urlForPage = pageUrl
                )

                repo.set(updated).map { _ =>
                  Ok(view(psd.schemeName, DashboardPage.nextPage(updated).url, vm))
                }
            }
          case None      =>
            logger.warn(s"[DashboardController][onPageLoad] Missing PensionSchemeDetails in dashboard data for $id")
            Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        }
      case None     =>
        logger.warn(s"[DashboardController][onPageLoad] No dashboard data found for $id")
        Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
    }
  }
}

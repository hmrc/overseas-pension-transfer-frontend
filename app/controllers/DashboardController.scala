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

import connectors.TransferConnector
import controllers.actions.IdentifierAction
import pages.DashboardPage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._
import queries.PensionSchemeDetailsQuery
import repositories.DashboardSessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DashboardView

import javax.inject._
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

@Singleton
class DashboardController @Inject() (
    val controllerComponents: MessagesControllerComponents,
    repo: DashboardSessionRepository,
    transferConnector: TransferConnector,
    identify: IdentifierAction,
    view: DashboardView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad: Action[AnyContent] = identify.async { implicit request =>
    val id = request.authenticatedUser.internalId

    repo.get(id).map {
      case Some(dd) =>
        dd.get(PensionSchemeDetailsQuery) match {
          case Some(psd) => {
            transferConnector.getAllTransfers(psd.pstrNumber).onComplete {
              case Success(Right(dto)) =>
                logger.info(s"[DashboardController][onPageLoad] getAllTransfers: pstr=${dto.pstr.value}, count=${dto.transfers.size}")
                logger.info(Json.prettyPrint(Json.toJson(dto.transfers.head)))
              case Success(Left(err))  =>
                logger.warn(s"[DashboardController][onPageLoad] getAllTransfers failed: $err")
              case Failure(ex)         =>
                logger.error(s"[DashboardController][onPageLoad] getAllTransfers exception", ex)
            }
            Ok(view(psd.schemeName, DashboardPage.nextPage(dd).url))
          }
          case None      =>
            logger.warn(s"[DashboardController][onPageLoad] Missing PensionSchemeDetails in dashboard data for $id")
            Redirect(routes.JourneyRecoveryController.onPageLoad())
        }
      case None     =>
        logger.warn(s"[DashboardController][onPageLoad] No dashboard data found for $id")
        Redirect(routes.JourneyRecoveryController.onPageLoad())
    }
  }
}

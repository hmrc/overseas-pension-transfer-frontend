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

import controllers.actions.IdentifierAction
import pages.DashboardPage
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.DashboardSessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DashboardView

import javax.inject._
import scala.concurrent.ExecutionContext

@Singleton
class DashboardController @Inject() (
    val controllerComponents: MessagesControllerComponents,
    repo: DashboardSessionRepository,
    identify: IdentifierAction,
    view: DashboardView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = identify.async { implicit request =>
    val id = request.authenticatedUser.internalId

    repo.get(id).map {
      case Some(dd) =>
        Ok(view(DashboardPage.nextPage(dd).url))
      case None     =>
        Redirect(routes.JourneyRecoveryController.onPageLoad())
    }
  }
}

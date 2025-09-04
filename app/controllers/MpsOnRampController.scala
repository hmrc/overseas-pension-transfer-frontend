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
import models.{DashboardData, PstrNumber, SrnNumber}
import pages.MpsOnRampPage

import javax.inject._
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import play.api.i18n.I18nSupport
import queries.mps.{PstrQuery, ReturnUrlQuery, SrnQuery}
import repositories.DashboardSessionRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MpsOnRampController @Inject() (
    val controllerComponents: MessagesControllerComponents,
    repo: DashboardSessionRepository,
    identify: IdentifierAction
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  def onRamp(pstr: String, srn: String, returnUrl: String): Action[AnyContent] = identify.async { implicit request =>
    for {
      dashboardData <- Future.fromTry(new DashboardData(request.authenticatedUser.internalId).set(PstrQuery, PstrNumber(pstr)))
      dd1           <- Future.fromTry(dashboardData.set(ReturnUrlQuery, returnUrl))
      dd2           <- Future.fromTry(dd1.set(SrnQuery, SrnNumber(srn)))
      _             <- repo.set(dd2)
    } yield Redirect(MpsOnRampPage.nextPage(dd2))
  }
}

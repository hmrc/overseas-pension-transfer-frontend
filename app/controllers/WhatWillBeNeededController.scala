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
import models.{NormalMode, PstrNumber, SrnNumber, UserAnswers}
import pages.WhatWillBeNeededPage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.WhatWillBeNeededView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WhatWillBeNeededController @Inject() (
    val controllerComponents: MessagesControllerComponents,
    identify: IdentifierAction,
    view: WhatWillBeNeededView,
    sessionRepository: SessionRepository
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(pstr: String, srn: String): Action[AnyContent] = identify.async { implicit request =>
    val id = request.authenticatedUser.internalId

    sessionRepository.get(id).flatMap {
      case Some(existing) =>
        Future.successful(Ok(view(WhatWillBeNeededPage.nextPage(NormalMode, existing).url)))

      case None =>
        for {
          newUa     <- Future.fromTry(UserAnswers.initialise(id))
          persisted <- sessionRepository.set(newUa)
        } yield {
          if (persisted) {
            Ok(view(WhatWillBeNeededPage.nextPage(NormalMode, newUa).url))
          } else {
            logger.warn("SessionRepository.set returned false during UA initialisation")
            Redirect(routes.JourneyRecoveryController.onPageLoad())
          }
        }
    } recover { case e =>
      logger.warn("Failed to initialise UA with defaults", e)
      Redirect(routes.JourneyRecoveryController.onPageLoad())
    }
  }
}

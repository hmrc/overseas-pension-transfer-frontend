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
import models.{DashboardData, NormalMode, PstrNumber, SrnNumber, UserAnswers}
import pages.{MpsOnRampPage, WhatWillBeNeededPage}
import play.api.Logging

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
    dashboardRepo: DashboardSessionRepository,
    identify: IdentifierAction
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with Logging {

  def onRamp(srn: String): Action[AnyContent] = identify.async { implicit request =>
    (for {
      dd        <- Future.fromTry(DashboardData(request.authenticatedUser.internalId).set(SrnQuery, SrnNumber(srn)))
      persisted <- dashboardRepo.set(dd)
    } yield {
      if (persisted) {
        Redirect(MpsOnRampPage.nextPage(dd))
      } else {
        logger.warn("Dashboard repo set returned false")
        Redirect(routes.JourneyRecoveryController.onPageLoad())
      }
    }).recover { case t =>
      logger.error("Failed to persist dashboard data", t)
      InternalServerError
    }
  }

}

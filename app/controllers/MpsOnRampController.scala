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

import queries.PensionSchemeDetailsQuery
import play.api.mvc._
import pages.MpsOnRampPage
import controllers.actions.IdentifierAction
import controllers.actions.SchemeDataAction
import play.api.Logging
import repositories.DashboardSessionRepository
import models.DashboardData
import play.api.i18n.I18nSupport
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import java.time.Clock
import java.time.Instant
import javax.inject._

@Singleton
class MpsOnRampController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  dashboardRepo: DashboardSessionRepository,
  identify: IdentifierAction,
  schemeData: SchemeDataAction,
  clock: Clock
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onRamp(srn: String): Action[AnyContent] = (identify andThen schemeData).async { implicit request =>
    val dashboardData = DashboardData.create(request.authenticatedUser.internalId, Instant.now(clock))

    Future
      .fromTry(dashboardData.set(PensionSchemeDetailsQuery, request.schemeDetails))
      .flatMap { dd1 =>
        dashboardRepo.set(dd1).map { persisted =>
          if (persisted) {
            Redirect(MpsOnRampPage.nextPage(dd1))
          } else {
            logger.warn("[MpsOnRampController][onRamp] dashboardRepo.set returned false")
            Redirect(MpsOnRampPage.nextPageRecovery())
          }
        }
      }
      .recover { case t =>
        logger.error("[MpsOnRampController][onRamp] Failed while setting/persisting dashboard data", t)
        Redirect(MpsOnRampPage.nextPageRecovery())
      }
  }
}

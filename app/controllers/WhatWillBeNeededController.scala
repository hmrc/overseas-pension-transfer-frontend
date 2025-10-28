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

import controllers.actions.{IdentifierAction, SchemeDataAction}
import models.audit.JourneyStartedType.StartNewTransfer
import models.audit.ReportStartedAuditModel
import models.{NormalMode, PstrNumber, SessionData, SrnNumber, TransferNumber, UserAnswers}
import pages.WhatWillBeNeededPage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{AuditService, UserAnswersService}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.WhatWillBeNeededView

import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WhatWillBeNeededController @Inject() (
    val controllerComponents: MessagesControllerComponents,
    identify: IdentifierAction,
    schemeData: SchemeDataAction,
    view: WhatWillBeNeededView,
    sessionRepository: SessionRepository,
    userAnswersService: UserAnswersService,
    auditService: AuditService
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen schemeData).async { implicit request =>
    // TODO remove .get
    val sessionData = SessionData(
      request.authenticatedUser.internalId,
      transferId = TransferNumber(UUID.randomUUID().toString),
      request.authenticatedUser.pensionSchemeDetails.get,
      request.authenticatedUser,
      Json.obj()
    )

    val newUa = UserAnswers(sessionData.transferId, sessionData.schemeInformation.pstrNumber)

    for {
      updatedSessionData <- Future.fromTry(SessionData.initialise(sessionData))
      persisted          <- sessionRepository.set(updatedSessionData)
      _                  <- userAnswersService.setExternalUserAnswers(newUa)
    } yield {
      if (persisted) {
        auditService.audit(
          ReportStartedAuditModel(
            sessionData.transferId,
            request.authenticatedUser,
            StartNewTransfer,
            None,
            None
          )
        )

        Ok(view(WhatWillBeNeededPage.nextPage(NormalMode, newUa).url))
      } else {
        logger.warn("SessionRepository.set returned false during SessionData initialisation")
        Redirect(routes.JourneyRecoveryController.onPageLoad())
      }
    }
  }
}

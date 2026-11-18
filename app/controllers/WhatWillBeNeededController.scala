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
import services.{LockService, UserAnswersService}
import models.authentication.{PsaUser, PspUser}
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.MessagesControllerComponents
import pages.WhatWillBeNeededPage
import views.html.WhatWillBeNeededView
import controllers.actions.IdentifierAction
import controllers.actions.SchemeDataAction
import repositories.SessionRepository
import play.api.Logging
import play.api.libs.json.Json
import models.*
import models.audit.JourneyStartedType.StartNewTransfer
import models.requests.SchemeRequest
import play.api.i18n.I18nSupport
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.ExecutionContext
import java.time.Clock
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WhatWillBeNeededController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  identify: IdentifierAction,
  schemeData: SchemeDataAction,
  view: WhatWillBeNeededView,
  sessionRepository: SessionRepository,
  userAnswersService: UserAnswersService,
  lockService: LockService,
  clock: Clock
)(implicit ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val lockTtlSeconds: Long = appConfig.dashboardLockTtl

  private def owner(implicit request: SchemeRequest[AnyContent]): String = request.authenticatedUser match {
    case PsaUser(psaId, _, _) => psaId.value
    case PspUser(pspId, _, _) => pspId.value
  }

  def onPageLoad(): Action[AnyContent] = (identify andThen schemeData) { implicit request =>
    Ok(view())
  }

  def onSubmit(): Action[AnyContent] = (identify andThen schemeData).async { implicit request =>
    val sessionData = SessionData(
      request.authenticatedUser.internalId,
      transferId = TransferNumber(UUID.randomUUID().toString),
      request.schemeDetails,
      request.authenticatedUser,
      Json.obj(),
      Instant.now(clock)
    )

    val newUa =
      UserAnswers(sessionData.transferId, sessionData.schemeInformation.pstrNumber, Json.obj(), Instant.now(clock))

    for {
      lockAcquired <- lockService.takeLockWithAudit(
                        sessionData.transferId,
                        owner,
                        lockTtlSeconds,
                        request.authenticatedUser,
                        request.schemeDetails,
                        StartNewTransfer,
                        None
                      )
      persisted    <- sessionRepository.set(sessionData)
      _            <- userAnswersService.setExternalUserAnswers(newUa, sessionData.schemeInformation.srnNumber)
    } yield
      if (!lockAcquired) {
        Redirect(routes.DashboardController.onPageLoad())
      } else if (persisted) {
        Redirect(WhatWillBeNeededPage.nextPage(NormalMode, newUa))
      } else {
        logger.warn("SessionRepository.set returned false during SessionData initialisation")
        Redirect(WhatWillBeNeededPage.nextPageRecovery())
      }
  }
}

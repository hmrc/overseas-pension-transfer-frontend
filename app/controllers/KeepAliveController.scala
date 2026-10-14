/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.MessagesControllerComponents
import controllers.actions.DataRetrievalAction
import controllers.actions.IdentifierAction
import controllers.actions.SchemeDataAction
import play.api.Logging
import repositories.{ExpiringMongoLockRepository, SessionRepository}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}
import javax.inject.Inject

class KeepAliveController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  identify: IdentifierAction,
  getScheme: SchemeDataAction,
  getData: DataRetrievalAction,
  sessionRepository: SessionRepository,
  lockRepository: ExpiringMongoLockRepository
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with Logging {

  def keepAlive(): Action[AnyContent] = (identify andThen getScheme andThen getData).async { implicit request =>
    sessionRepository.get(request.authenticatedUser.internalId) flatMap {
      case Some(value) =>
        val lockId = value.transferId.value
        val owner  = request.authenticatedUser.owner()

        for {
          _         <- lockRepository.refreshExpiry(lockId, owner)
          keepAlive <- sessionRepository.keepAlive(request.userAnswers.id.value).map(_ => Ok)
        } yield keepAlive
      case None        =>
        logger.error("No Session Data found")
        Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
    }
  }
}

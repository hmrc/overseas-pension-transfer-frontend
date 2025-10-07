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

package controllers.actions

import controllers.routes
import models.requests.{DisplayRequest, IdentifierRequest}
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.Results.{BadRequest, Redirect}
import play.api.mvc.{ActionRefiner, Result}
import repositories.SessionRepository
import services.UserAnswersService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.AppUtils

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DataRetrievalActionImpl @Inject() (
    sessionRepository: SessionRepository,
    userAnswersService: UserAnswersService
  )(implicit val executionContext: ExecutionContext
  ) extends DataRetrievalAction with AppUtils with Logging {

  override protected def refine[A](request: IdentifierRequest[A]): Future[Either[Result, DisplayRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

    sessionRepository.get(request.authenticatedUser.internalId) flatMap {
      case Some(value) =>
        userAnswersService.getExternalUserAnswers(value) map {
          case Right(answers) =>
            println("\n----\nanswers = " + Json.prettyPrint(Json.toJson(answers)) + "\n----\n")
            Right(DisplayRequest(
              request.request,
              request.authenticatedUser,
              answers,
              value,
              memberFullName(answers),
              qtNumber(value),
              dateTransferSubmitted(value)
            ))
          case Left(_)        => Left(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        }
      case None        => Future.successful(Left(Redirect(routes.JourneyRecoveryController.onPageLoad())))
    }

  }
}

trait DataRetrievalAction extends ActionRefiner[IdentifierRequest, DisplayRequest]

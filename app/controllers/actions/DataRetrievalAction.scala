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
import models.requests.{DisplayRequest, GetSpecificData, GetSpecificDataParser, IdentifierRequest}
import models.{SessionData, UserAnswers}
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

    GetSpecificDataParser.fromRequest(request.request) match {
      case Left(msg) =>
        Future.successful(Left(BadRequest(msg)))

      case Right(Some(spec: GetSpecificData)) =>
        userAnswersService.getExternalUserAnswers(spec).map {
          case Right(answers) =>
            val session = SessionData(
              request.authenticatedUser.internalId,
              spec.transferReference.orElse(spec.qtNumber.map(_.value)).get,
              request.authenticatedUser.pensionSchemeDetails.get,
              request.authenticatedUser,
              Json.toJsObject(answers)
            )
            Right(buildDisplayRequest(request, answers, session))
          case Left(_)        =>
            Left(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        }
      case Right(None)                        =>
        sessionRepository.get(request.authenticatedUser.internalId) flatMap {
          case Some(session) =>
            userAnswersService.getExternalUserAnswers(session) map {
              case Right(answers) =>
                Right(buildDisplayRequest(request, answers, session))
              case Left(_)        => Left(Redirect(routes.JourneyRecoveryController.onPageLoad()))
            }
          case None          => Future.successful(Left(Redirect(routes.JourneyRecoveryController.onPageLoad())))
        }
    }
  }

  private def buildDisplayRequest[A](
      request: IdentifierRequest[A],
      answers: UserAnswers,
      session: SessionData
    ): DisplayRequest[A] =
    DisplayRequest(
      request.request,
      request.authenticatedUser,
      answers,
      session,
      memberFullName(answers),
      qtNumber(session),
      dateTransferSubmitted(session)
    )
}

trait DataRetrievalAction extends ActionRefiner[IdentifierRequest, DisplayRequest]

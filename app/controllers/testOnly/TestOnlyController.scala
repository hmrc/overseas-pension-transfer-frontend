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

package controllers.testOnly

import com.google.inject.Inject
import connectors.UserAnswersConnector
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.ExecutionContext

class TestOnlyController @Inject() (
    sessionRepository: SessionRepository,
    userAnswersConnector: UserAnswersConnector,
    cc: ControllerComponents
  )(implicit ec: ExecutionContext
  ) extends AbstractController(cc) {

  def resetDatabase: Action[AnyContent] = Action.async {
    implicit request =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
      sessionRepository.clear flatMap {
        _ =>
          userAnswersConnector.resetDatabase map {
            response =>
              response.status match {
                case NO_CONTENT => Ok("Success")
                case _          => BadGateway("Reset failed. Try again")
              }
          }
      }
  }
}

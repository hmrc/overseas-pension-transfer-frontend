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

import base.SpecBase
import models.requests.{DisplayRequest, IdentifierRequest}
import models.responses.UserAnswersErrorResponse
import models.{PensionSchemeDetails, PstrNumber, SessionData, SrnNumber, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.SEE_OTHER
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import repositories.SessionRepository
import services.UserAnswersService
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataRetrievalActionSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val sessionData = SessionData(
    "sessionId",
    "transferId",
    PensionSchemeDetails(
      SrnNumber("12345"),
      PstrNumber("12345678AB"),
      "Scheme Name"
    ),
    psaUser,
    Json.obj(),
    Instant.now
  )

  class Harness(sessionRepository: SessionRepository, userAnswersService: UserAnswersService)
      extends DataRetrievalActionImpl(sessionRepository, userAnswersService) {
    def callRefine[A](request: IdentifierRequest[A]): Future[Either[Result, DisplayRequest[A]]] = refine(request)
  }

  "Data Retrieval Action" - {

    "when there is no session data in the cache" - {

      "must redirect to JourneyRecovery" in {

        val sessionRepository  = mock[SessionRepository]
        val userAnswersService = mock[UserAnswersService]

        when(sessionRepository.get(any())).thenReturn(Future.successful(None))

        val action = new Harness(sessionRepository, userAnswersService)

        val futureResult = action.callRefine(IdentifierRequest(FakeRequest(), psaUser)).futureValue

        futureResult.left.map {
          result =>
            result.header.status mustBe SEE_OTHER
            result.header.headers.get("Location") mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
        }
      }
    }

    "when there is no data returned from user answers service" - {
      "must redirect to JourneyRecovery" in {

        val sessionRepository  = mock[SessionRepository]
        val userAnswersService = mock[UserAnswersService]

        when(sessionRepository.get(any())).thenReturn(Future.successful(Some(sessionData)))
        when(userAnswersService.getExternalUserAnswers(any())(any())).thenReturn(Future.successful(Left(UserAnswersErrorResponse("Error", None))))

        val action = new Harness(sessionRepository, userAnswersService)

        val futureResult = action.callRefine(IdentifierRequest(FakeRequest(), psaUser)).futureValue

        futureResult.left.map {
          result =>
            result.header.status mustBe SEE_OTHER
            result.header.headers.get("Location") mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
        }
      }
    }

    "when there is data in the cache" - {

      "must build a userAnswers, memberName, qtNumber and dateTransferSubmitted object and add it to the request" in {
        val userAnswers = UserAnswers("id", PstrNumber("12345678AB"))

        val sessionRepository  = mock[SessionRepository]
        val userAnswersService = mock[UserAnswersService]

        when(sessionRepository.get("id")) thenReturn Future(Some(sessionData))
        when(userAnswersService.getExternalUserAnswers(any())(any())).thenReturn(Future.successful(Right(userAnswers)))
        val action = new Harness(sessionRepository, userAnswersService)

        val result = action.callRefine(IdentifierRequest(FakeRequest(), psaUser)).futureValue

        result.map {
          displayRequest =>
            displayRequest.userAnswers mustBe userAnswers
        }
      }
    }
  }
}

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

package controllers.actions

import base.SpecBase
import models.requests.{DisplayRequest, GetSpecificData, IdentifierRequest}
import models.responses.UserAnswersErrorResponse
import models.{PensionSchemeDetails, PstrNumber, SessionData, SrnNumber, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.{BAD_REQUEST, SEE_OTHER}
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
    sessionId         = "session-internal-id",
    transferId        = "transferId-123",
    schemeInformation = PensionSchemeDetails(
      SrnNumber("12345"),
      PstrNumber("12345678AB"),
      "Scheme Name"
    ),
    user              = psaUser,
    data              = Json.obj(),
    lastUpdated       = Instant.now
  )

  class Harness(sessionRepository: SessionRepository, userAnswersService: UserAnswersService)
      extends DataRetrievalActionImpl(sessionRepository, userAnswersService) {
    def callRefine[A](request: IdentifierRequest[A]): Future[Either[Result, DisplayRequest[A]]] = refine(request)
  }

  "Data Retrieval Action" - {

    "when there is no session data in the cache and no query params" - {
      "must redirect to JourneyRecovery" in {
        val sessionRepository  = mock[SessionRepository]
        val userAnswersService = mock[UserAnswersService]

        when(sessionRepository.get(any[String]())).thenReturn(Future.successful(None))

        val action = new Harness(sessionRepository, userAnswersService)

        val futureResult = action.callRefine(IdentifierRequest(FakeRequest(), psaUser)).futureValue

        futureResult.left.map { result =>
          result.header.status mustBe SEE_OTHER
          result.header.headers.get("Location") mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
        }
      }
    }

    "when GetSpecificDataParser yields an error (bad/missing params)" - {
      "must return BadRequest" in {
        val sessionRepository  = mock[SessionRepository]
        val userAnswersService = mock[UserAnswersService]

        val req = FakeRequest("GET", "/some/path?transferReference=TR-1&qtStatus=Submitted")

        val action = new Harness(sessionRepository, userAnswersService)
        val result = action.callRefine(IdentifierRequest(req, psaUser)).futureValue

        result.left.map { r =>
          r.header.status mustBe BAD_REQUEST
        }
      }
    }

    "when GetSpecificData is present via query params" - {

      "and UA service returns Left -> must redirect to JourneyRecovery" in {
        val sessionRepository  = mock[SessionRepository]
        val userAnswersService = mock[UserAnswersService]

        val req = FakeRequest(
          "GET",
          "/some/path?transferReference=TR-123&pstr=12345678AB&qtStatus=Submitted"
        )

        when(userAnswersService.getExternalUserAnswers(any[GetSpecificData]())(any[HeaderCarrier]()))
          .thenReturn(Future.successful(Left(UserAnswersErrorResponse("boom", None))))

        val action = new Harness(sessionRepository, userAnswersService)
        val out    = action.callRefine(IdentifierRequest(req, psaUser)).futureValue

        out.left.map { result =>
          result.header.status mustBe SEE_OTHER
          result.header.headers.get("Location") mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
        }
      }

      "and UA service returns Right -> must build DisplayRequest with answers" in {
        val sessionRepository  = mock[SessionRepository]
        val userAnswersService = mock[UserAnswersService]

        val req = FakeRequest(
          "GET",
          "/some/path?transferReference=TR-123&pstr=12345678AB&qtStatus=Submitted&versionNumber=7"
        )

        val ua = UserAnswers(id = "TR-123", pstr = PstrNumber("12345678AB"))

        when(userAnswersService.getExternalUserAnswers(any[GetSpecificData]())(any[HeaderCarrier]()))
          .thenReturn(Future.successful(Right(ua)))

        val userWithScheme = psaUser.copy(
          pensionSchemeDetails = Some(
            PensionSchemeDetails(
              SrnNumber("12345"),
              PstrNumber("12345678AB"),
              "Scheme Name"
            )
          )
        )

        val action  = new Harness(sessionRepository, userAnswersService)
        val outcome = action.callRefine(IdentifierRequest(req, userWithScheme)).futureValue

        outcome.map { dr =>
          dr.userAnswers mustBe ua
          dr.memberName.nonEmpty mustBe true
        }
      }
    }

    "when there is no GetSpecificData (no query params) but there is data in the cache" - {

      "and UA service returns Left -> must redirect to JourneyRecovery" in {
        val sessionRepository  = mock[SessionRepository]
        val userAnswersService = mock[UserAnswersService]

        when(sessionRepository.get(any[String]()))
          .thenReturn(Future.successful(Some(sessionData)))

        when(userAnswersService.getExternalUserAnswers(any[SessionData]())(any[HeaderCarrier]()))
          .thenReturn(Future.successful(Left(UserAnswersErrorResponse("Error", None))))

        val action = new Harness(sessionRepository, userAnswersService)
        val out    = action.callRefine(IdentifierRequest(FakeRequest(), psaUser)).futureValue

        out.left.map { result =>
          result.header.status mustBe SEE_OTHER
          result.header.headers.get("Location") mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
        }
      }

      "and UA service returns Right -> must build DisplayRequest with answers" in {
        val sessionRepository  = mock[SessionRepository]
        val userAnswersService = mock[UserAnswersService]

        val ua = UserAnswers(id = "transferId-123", pstr = PstrNumber("12345678AB"))

        when(sessionRepository.get(any[String]()))
          .thenReturn(Future.successful(Some(sessionData)))

        when(userAnswersService.getExternalUserAnswers(any[SessionData]())(any[HeaderCarrier]()))
          .thenReturn(Future.successful(Right(ua)))

        val action  = new Harness(sessionRepository, userAnswersService)
        val outcome = action.callRefine(IdentifierRequest(FakeRequest(), psaUser)).futureValue

        outcome.map { dr =>
          dr.userAnswers mustBe ua
        }
      }
    }
  }
}

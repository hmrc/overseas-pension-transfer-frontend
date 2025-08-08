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

package connectors

import base.BaseISpec
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, stubFor}
import models.QtNumber
import models.authentication.{PsaId, Psp, PspId}
import models.dtos.{PspSubmissionDTO, SubmissionDTO, UserAnswersDTO}
import models.responses.{SubmissionErrorResponse, SubmissionResponse, UserAnswersErrorResponse, UserAnswersNotFoundResponse}
import org.apache.pekko.Done
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.test.Injecting

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global

class UserAnswersConnectorISpec extends BaseISpec with Injecting {

  private val instant        = Instant.now()
  private val userAnswersDTO = UserAnswersDTO("testId", JsObject(Map("field" -> JsString("value"))), instant)
  private val submissionDTO  = PspSubmissionDTO("testId", Psp, PspId("X1234567"), PsaId("a1234567"), instant)

  val connector: UserAnswersConnector = inject[UserAnswersConnector]

  "getAnswers" should {
    "return UserAnswersSuccessResponse when 200 returned with UserAnswers payload" in {
      stubGet(
        "/overseas-pension-transfer-backend/save-for-later/testId",
        Json.toJson(userAnswersDTO).toString()
      )

      val getAnswers = await(connector.getAnswers("testId"))

      getAnswers shouldBe Right(userAnswersDTO)
    }

    "return UserAnswersNotFoundResponse when 404 is returned" in {
      stubGet(
        "/overseas-pension-transfer-backend/save-for-later/testId",
        """
          |{
          | "error": "NotFoundError"
          |}
          |""".stripMargin,
        NOT_FOUND
      )

      val getAnswers = await(connector.getAnswers("testId"))

      getAnswers shouldBe Left(UserAnswersNotFoundResponse)
    }

    "return UserAnswersErrorResponse" when {
      "500 is returned" in {
        stubGet(
          "/overseas-pension-transfer-backend/save-for-later/testId",
          """{"error": "InternalServerError", "details": "Where the extra details come from"}""",
          INTERNAL_SERVER_ERROR
        )

        val getAnswers = await(connector.getAnswers("testId"))

        getAnswers shouldBe Left(UserAnswersErrorResponse("InternalServerError", Some("Where the extra details come from")))
      }

      "200 returned with invalid payload" in {
        stubGet(
          "/overseas-pension-transfer-backend/save-for-later/testId",
          """{"field": "value"}""",
          OK
        )

        val getAnswers = await(connector.getAnswers("testId"))

        getAnswers shouldBe Left(UserAnswersErrorResponse("Unable to parse Json as UserAnswersDTO", Some("/data | /lastUpdated | /referenceId")))
      }

      "500 returned with invalid payload" in {
        stubGet(
          "/overseas-pension-transfer-backend/save-for-later/testId",
          """{"field": "value"}""",
          INTERNAL_SERVER_ERROR
        )

        val getAnswers = await(connector.getAnswers("testId"))

        getAnswers shouldBe Left(UserAnswersErrorResponse("Unable to parse Json as UserAnswersErrorResponse", Some("/error")))
      }
    }
  }

  "setAnswers" should {
    "return UserAnswerSaveSuccessfulResponse when 204 is returned" in {
      stubFor(post("/overseas-pension-transfer-backend/save-for-later/testId")
        .willReturn(
          aResponse()
            .withStatus(NO_CONTENT)
        ))

      val putAnswers = await(connector.putAnswers(userAnswersDTO))

      putAnswers shouldBe Right(Done)
    }

    "return UserAnswersErrorResponse" when {
      "400 is returned" in {
        stubPost(
          "/overseas-pension-transfer-backend/save-for-later/testId",
          """{"error": "Transformation failed", "details": "Payload received is invalid"}""",
          BAD_REQUEST
        )

        val putAnswers = await(connector.putAnswers(userAnswersDTO))

        putAnswers shouldBe Left(UserAnswersErrorResponse("Transformation failed", Some("Payload received is invalid")))
      }

      "500 is returned" in {
        stubPost(
          "/overseas-pension-transfer-backend/save-for-later/testId",
          """{"error": "Failed to save answers"}""",
          INTERNAL_SERVER_ERROR
        )

        val putAnswers = await(connector.putAnswers(userAnswersDTO))

        putAnswers shouldBe Left(UserAnswersErrorResponse("Failed to save answers", None))
      }
    }
  }

  "postSubmission" should {

    "return SubmissionResponse when 200 is returned" in {
      stubFor(post("/overseas-pension-transfer-backend/submit-declaration/testId")
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withHeader("Content-Type", "application/json")
            .withBody("""{ "qtNumber": { "value": "QT123456" } }""")
        ))

      val response = await(connector.postSubmission(submissionDTO))

      response shouldBe Right(SubmissionResponse(QtNumber("QT123456")))
    }

    "return SubmissionErrorResponse" when {

      "400 is returned" in {
        stubPost(
          "/overseas-pension-transfer-backend/submit-declaration/testId",
          """{ "error": "Transformation failed", "details": "Payload received is invalid" }""",
          BAD_REQUEST
        )

        val response = await(connector.postSubmission(submissionDTO))

        response shouldBe Left(SubmissionErrorResponse("Transformation failed", Some("Payload received is invalid")))
      }

      "500 is returned" in {
        stubPost(
          "/overseas-pension-transfer-backend/submit-declaration/testId",
          """{ "error": "Failed to save answers" }""",
          INTERNAL_SERVER_ERROR
        )

        val response = await(connector.postSubmission(submissionDTO))

        response shouldBe Left(SubmissionErrorResponse("Failed to save answers", None))
      }
    }
  }

}

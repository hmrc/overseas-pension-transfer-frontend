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
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, delete, post, stubFor}
import models.QtStatus.{InProgress, Submitted}
import models.{PstrNumber, QtNumber, TransferNumber}
import models.authentication.{PsaId, Psp, PspId}
import models.dtos.{PspSubmissionDTO, UserAnswersDTO}
import models.responses.{SubmissionErrorResponse, SubmissionResponse, UserAnswersErrorResponse, UserAnswersNotFoundResponse}
import org.apache.pekko.Done
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.test.Injecting
import stubs.TransferBackendStub

import java.time.Instant
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

class UserAnswersConnectorISpec extends BaseISpec with Injecting {

  private val transferId = TransferNumber(UUID.randomUUID().toString)
  private val instant        = Instant.now()
  private val pstr = PstrNumber("12345678AB")
  private val userAnswersDTO = UserAnswersDTO(QtNumber("QT975310"), pstr, JsObject(Map("field" -> JsString("value"))), instant)
  private val submissionDTO  = PspSubmissionDTO(transferId, Psp, PspId("X1234567"), PsaId("a1234567"), instant)

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

        getAnswers shouldBe Left(UserAnswersErrorResponse("Unable to parse Json as UserAnswersDTO", Some("/pstr | /transferId | /data | /lastUpdated")))
      }

      "500 returned with invalid payload" in {
        stubGet(
          "/overseas-pension-transfer-backend/save-for-later/testId",
          """{"field": "value"}""",
          INTERNAL_SERVER_ERROR
        )

        val getAnswers = await(connector.getAnswers("testId"))

        getAnswers shouldBe Left(
          UserAnswersErrorResponse(
            "[[UserAnswersConnector][getAnswers]] 500 Unknown (correlationId=-)",
            Some("""{"field": "value"}""")
          )
        )
      }
    }
  }


  "getAnswers (getSpecific)" when {

    val referenceId = QtNumber("QT123456")
    val now         = Instant.parse("2025-09-24T10:00:00Z")

    "the backend returns 200" must {

      "return Right(UserAnswersDTO) when qtStatus is InProgress and body is valid" in {
        TransferBackendStub.getSpecificTransferOk(
          referenceId    = referenceId.value,
          pstr           = pstr.value,
          qtStatus       = InProgress.toString,
          dataJson       = """{ "foo": "bar", "n": 1 }""",
          lastUpdatedIso = now.toString
        )

        val result = await(
          connector.getAnswers(
            transferId = referenceId,
            pstrNumber        = pstr,
            qtStatus          = InProgress,
            versionNumber     = None
          )
        )

        result match {
          case Right(dto) =>
            dto.referenceId    shouldBe referenceId
            dto.pstr.value     shouldBe pstr.value
            dto.data.toString  should include ("foo")
            dto.lastUpdated    shouldBe now
          case Left(err)  =>
            fail(s"Expected Right(UserAnswersDTO) but got $err")
        }
      }

      "return Right(UserAnswersDTO) when qtStatus is Submitted and versionNumber provided" in {
        TransferBackendStub.getSpecificTransferOk(
          referenceId    = referenceId.value,
          pstr           = pstr.value,
          qtStatus       = Submitted.toString,
          dataJson       = """{ "submitted": true, "hasVersion": true }""",
          lastUpdatedIso = now.toString,
          versionNumber  = Some("002")
        )

        val result = await(
          connector.getAnswers(
            transferId = referenceId,
            pstrNumber        = pstr,
            qtStatus          = Submitted,
            versionNumber     = Some("002")
          )
        )

        result match {
          case Right(dto) =>
            dto.referenceId   shouldBe referenceId
            dto.data.toString should include ("hasVersion")
          case Left(err)  =>
            fail(s"Expected Right(UserAnswersDTO) but got $err")
        }
      }

      "map to Left(UserAnswersErrorResponse) if body is malformed JSON" in {
        TransferBackendStub.getSpecificTransferMalformed(
          referenceId = referenceId.value,
          pstr        = pstr.value,
          qtStatus    = Submitted.toString
        )

        val result = await(
          connector.getAnswers(
            transferId = referenceId,
            pstrNumber        = pstr,
            qtStatus          = Submitted,
            versionNumber     = None
          )
        )

        result match {
          case Left(_: UserAnswersErrorResponse) => succeed
          case other                              => fail(s"Expected UserAnswersErrorResponse but got $other")
        }
      }
    }

    "the backend returns 404" must {

      "map to Left(UserAnswersNotFoundResponse)" in {
        TransferBackendStub.getSpecificTransferNotFound(
          referenceId = referenceId.value,
          pstr        = pstr.value,
          qtStatus    = Submitted.toString
        )

        val result = await(
          connector.getAnswers(
            transferId = referenceId,
            pstrNumber        = pstr,
            qtStatus          = Submitted,
            versionNumber     = None
          )
        )

        result shouldBe Left(UserAnswersNotFoundResponse)
      }
    }

    "the backend returns 500" must {

      "map to Left(UserAnswersErrorResponse)" in {
        TransferBackendStub.getSpecificTransferServerError(
          referenceId = referenceId.value,
          pstr        = pstr.value,
          qtStatus    = InProgress.toString
        )

        val result = await(
          connector.getAnswers(
            transferId = referenceId,
            pstrNumber        = pstr,
            qtStatus          = InProgress,
            versionNumber     = None
          )
        )

        result match {
          case Left(_: UserAnswersErrorResponse) => succeed
          case other                             => fail(s"Expected Left(UserAnswersErrorResponse) but got $other")
        }
      }
    }
  }

  "setAnswers" should {
    "return UserAnswerSaveSuccessfulResponse when 204 is returned" in {
      stubFor(post("/overseas-pension-transfer-backend/save-for-later")
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
          "/overseas-pension-transfer-backend/save-for-later",
          """{"error": "Transformation failed", "details": "Payload received is invalid"}""",
          BAD_REQUEST
        )

        val putAnswers = await(connector.putAnswers(userAnswersDTO))

        putAnswers shouldBe Left(UserAnswersErrorResponse("Transformation failed", Some("Payload received is invalid")))
      }

      "500 is returned" in {
        stubPost(
          "/overseas-pension-transfer-backend/save-for-later",
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
      stubFor(post(s"/overseas-pension-transfer-backend/submit-declaration/${transferId.value}")
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withHeader("Content-Type", "application/json")
            .withBody("""{ "qtNumber": "QT123456" }""")
        ))

      val response = await(connector.postSubmission(submissionDTO))

      response shouldBe Right(SubmissionResponse(QtNumber("QT123456")))
    }

    "return SubmissionErrorResponse" when {

      "400 is returned" in {
        stubPost(
          s"/overseas-pension-transfer-backend/submit-declaration/${transferId.value}",
          """{ "error": "Transformation failed", "details": "Payload received is invalid" }""",
          BAD_REQUEST
        )

        val response = await(connector.postSubmission(submissionDTO))

        response shouldBe Left(SubmissionErrorResponse("Transformation failed", Some("Payload received is invalid")))
      }

      "500 is returned" in {
        stubPost(
          s"/overseas-pension-transfer-backend/submit-declaration/${transferId.value}",
          """{ "error": "Failed to save answers" }""",
          INTERNAL_SERVER_ERROR
        )

        val response = await(connector.postSubmission(submissionDTO))

        response shouldBe Left(SubmissionErrorResponse("Failed to save answers", None))
      }
    }
  }

  "deleteAnswers" should {
    "return UserAnswerSaveSuccessfulResponse when 204 is returned" in {
      stubFor(delete(s"/overseas-pension-transfer-backend/save-for-later/testId")
        .willReturn(
          aResponse()
            .withStatus(NO_CONTENT)
        ))

      val putAnswers = await(connector.deleteAnswers("testId"))

      putAnswers shouldBe Right(Done)
    }

    "return UserAnswersErrorResponse" when {
      "500 is returned" in {
        stubFor(delete("/overseas-pension-transfer-backend/save-for-later/testId")
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
              .withBody( """{ "error": "Failed to save answers" }""")
          ))

        val putAnswers = await(connector.deleteAnswers("testId"))

        putAnswers shouldBe Left(UserAnswersErrorResponse("Failed to save answers", None))
      }
    }
  }

}

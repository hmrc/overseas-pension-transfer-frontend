package connectors

import base.BaseISpec
import models.dtos.UserAnswersDTO
import models.responses.{UserAnswersErrorResponse, UserAnswersNotFoundResponse, UserAnswersSuccessResponse}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.test.Injecting

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global

class UserAnswersConnectorISpec extends BaseISpec with Injecting {

  private val instant = Instant.now()

  val connector: UserAnswersConnector = inject[UserAnswersConnector]

  "getAnswers" should {
    "return UserAnswersSuccessResponse when 200 returned with UserAnswers payload" in {
      val userAnswersDTO = UserAnswersDTO("testId", JsObject(Map("field" -> JsString("value"))), instant)

      stubGet(
        "/save-for-later/testId",
        Json.toJson(userAnswersDTO).toString(),
        OK
      )

      val getAnswers = await(connector.getAnswers("testId"))

      getAnswers shouldBe UserAnswersSuccessResponse(userAnswersDTO)
    }

    "return UserAnswersNotFoundResponse when 404 is returned" in {
      stubGet(
        "/save-for-later/testId",
        """
          |{
          | "error": "NotFoundError"
          |}
          |""".stripMargin,
        NOT_FOUND
      )

      val getAnswers = await(connector.getAnswers("testId"))

      getAnswers shouldBe UserAnswersNotFoundResponse
    }

    "return UserAnswersErrorResponse" when {
      "500 is returned" in {
        stubGet(
          "/save-for-later/testId",
          """{"error": "InternalServerError", "details": "Where the extra details come from"}""",
          INTERNAL_SERVER_ERROR
        )

        val getAnswers = await(connector.getAnswers("testId"))

        getAnswers shouldBe UserAnswersErrorResponse("InternalServerError", Some("Where the extra details come from"))
      }

      "200 returned with invalid payload" in {
        stubGet(
          "/save-for-later/testId",
          """{"field": "value"}""",
          OK
        )

        val getAnswers = await(connector.getAnswers("testId"))

        getAnswers shouldBe UserAnswersErrorResponse("Unable to parse Json as UserAnswersDTO", Some(""))
      }

      "500 returned with invalid payload" in {
        stubGet(
          "/save-for-later/testId",
          """{"field": "value"}""",
          OK
        )

        val getAnswers = await(connector.getAnswers("testId"))

        getAnswers shouldBe UserAnswersErrorResponse("Unable to parse Json as UserAnswersErrorResponse", Some(""))
      }
    }
  }

  "setAnswers" should {
    "return UserAnswerSaveSuccessfulResponse when 204 is returned" in {

    }

    "return UserAnswersErrorResponse" when {
      "400 is returned" in {

      }

      "500 is returned" in {

      }
    }
  }
}

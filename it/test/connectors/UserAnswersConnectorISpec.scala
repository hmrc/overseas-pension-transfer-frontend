package connectors

import base.BaseISpec
import play.api.test.Injecting

class UserAnswersConnectorISpec extends BaseISpec with Injecting {

  val connector: UserAnswersConnector = inject[UserAnswersConnector]

  "getAnswers" should {
    "return UserAnswersSuccessResponse when 200 returned with UserAnswers payload" in {

    }

    "return UserAnswersNotFoundResponse when 404 is returned" in {

    }

    "return UserAnswersErrorResponse" when {
      "500 is returned" in {

      }

      "200 returned with invalid payload" in {

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

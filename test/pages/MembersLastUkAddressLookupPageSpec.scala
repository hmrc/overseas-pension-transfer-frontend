package pages

import controllers.routes
import models.{CheckMode, NormalMode, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class MembersLastUkAddressLookupPageSpec extends AnyFreeSpec with Matchers {

  ".nextPage" - {

    val emptyAnswers = UserAnswers("id")

    "in Normal Mode" - {

      "must go to Index" in {

        MembersLastUkAddressLookupPage.nextPage(NormalMode, emptyAnswers) mustEqual routes.IndexController.onPageLoad()
      }
    }

    "in Check Mode" - {

      "must go to Check Answers" in {

        MembersLastUkAddressLookupPage.nextPage(CheckMode, emptyAnswers) mustEqual routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}

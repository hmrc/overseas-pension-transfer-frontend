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

package pages.memberDetails

import controllers.memberDetails.routes
import models.{AmendCheckMode, CheckMode, FinalCheckMode, NormalMode, PstrNumber, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class MembersCurrentAddressPageSpec extends AnyFreeSpec with Matchers {

  ".nextPage" - {

    val emptyAnswers = UserAnswers("id", PstrNumber("12345678AB"))

    "in Normal Mode" - {

      "must go to Member Is UK Resident" in {

        MembersCurrentAddressPage.nextPage(NormalMode, emptyAnswers) mustEqual routes.MemberIsResidentUKController.onPageLoad(NormalMode)
      }
    }

    "in Check Mode" - {

      "must go to Check Answers" in {

        MembersCurrentAddressPage.nextPage(CheckMode, emptyAnswers) mustEqual routes.MemberDetailsCYAController.onPageLoad()
      }
    }

    "in FinalCheckMode" - {
      "must go to Final Check Answers page" in {
        MembersCurrentAddressPage.nextPage(FinalCheckMode, emptyAnswers) mustEqual
          controllers.checkYourAnswers.routes.CheckYourAnswersController.onPageLoad()
      }
    }

    "in AmendCheckMode" - {
      "must go to Amend Answers page" in {
        MembersCurrentAddressPage.nextPage(AmendCheckMode, emptyAnswers) mustEqual
          controllers.routes.ViewAmendSubmittedController.amend()
      }
    }
  }
}

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

import base.SpecBase
import controllers.memberDetails.routes
import models.{AmendCheckMode, CheckMode, FinalCheckMode, NormalMode, PstrNumber, UserAnswers}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

import java.time.LocalDate

class MembersLastUkAddressConfirmPageSpec extends AnyFreeSpec with Matchers with SpecBase {

  ".nextPage" - {

    val emptyAnswers = UserAnswers(userAnswersTransferNumber, PstrNumber("12345678AB"))

    "in Normal Mode" - {

      "must go to Members Date of Leaving UK" in {

        MembersLastUkAddressConfirmPage.nextPage(NormalMode, emptyAnswers) mustEqual routes.MemberDateOfLeavingUKController.onPageLoad(NormalMode)
      }
    }

    "in Check Mode" - {

      "must go to Check Answers if Members Date of Leaving UK present" in {
        val ua = emptyAnswers.set(MemberDateOfLeavingUKPage, LocalDate.now()).success.value
        MembersLastUkAddressConfirmPage.nextPage(CheckMode, ua) mustEqual routes.MemberDetailsCYAController.onPageLoad()
      }

      "must go to Members Date of Leaving UK in CheckMode if not present" in {
        MembersLastUkAddressConfirmPage.nextPage(CheckMode, emptyAnswers) mustEqual routes.MemberDateOfLeavingUKController.onPageLoad(CheckMode)
      }
    }

    "in FinalCheck Mode" - {

      "must go to FinalCheck Answers if Members Date of Leaving UK present" in {
        val ua = emptyAnswers.set(MemberDateOfLeavingUKPage, LocalDate.now()).success.value
        MembersLastUkAddressConfirmPage.nextPage(FinalCheckMode, ua) mustEqual controllers.checkYourAnswers.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go to Members Date of Leaving UK in FinalCheckMode if not present" in {
        MembersLastUkAddressConfirmPage.nextPage(FinalCheckMode, emptyAnswers) mustEqual routes.MemberDateOfLeavingUKController.onPageLoad(FinalCheckMode)
      }
    }

    "in AmendCheck Mode" - {

      "must go to AmendCheck Answers if Members Date of Leaving UK present" in {
        val ua = emptyAnswers.set(MemberDateOfLeavingUKPage, LocalDate.now()).success.value
        MembersLastUkAddressConfirmPage.nextPage(AmendCheckMode, ua) mustEqual controllers.routes.ViewAmendSubmittedController.amend()
      }

      "must go to Members Date of Leaving UK in AmendCheckMode if not present" in {
        MembersLastUkAddressConfirmPage.nextPage(AmendCheckMode, emptyAnswers) mustEqual routes.MemberDateOfLeavingUKController.onPageLoad(AmendCheckMode)
      }
    }
  }
}

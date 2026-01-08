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
import models.address.MembersLastUKAddress
import models.{AmendCheckMode, CheckMode, FinalCheckMode, NormalMode}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

import java.time.LocalDate

class MemberHasEverBeenResidentUKPageSpec extends AnyFreeSpec with SpecBase with Matchers {

  ".nextPage" - {

    emptyUserAnswers.set(MemberHasEverBeenResidentUKPage, false).success.value

    "in Normal Mode" - {

      "must go to Check Answers page when answer is 'false'" in {

        MemberHasEverBeenResidentUKPage.nextPage(
          NormalMode,
          emptyUserAnswers.set(MemberHasEverBeenResidentUKPage, false).success.value
        ) mustEqual routes.MemberDetailsCYAController.onPageLoad()
      }

      "must go to Member Last UK Address Lookup when answer is 'true'" in {

        MemberHasEverBeenResidentUKPage.nextPage(
          NormalMode,
          emptyUserAnswers.set(MemberHasEverBeenResidentUKPage, true).success.value
        ) mustEqual routes.MembersLastUKAddressController.onPageLoad(NormalMode)
      }
    }

    "in Check Mode" - {

      "must go to Check Answers page when answer is 'false'" in {

        MemberHasEverBeenResidentUKPage.nextPage(
          CheckMode,
          emptyUserAnswers.set(MemberHasEverBeenResidentUKPage, false).success.value
        ) mustEqual routes.MemberDetailsCYAController.onPageLoad()
      }

      "must go to Member Last UK Address Lookup in CheckMode when answer is 'true'" in {

        MemberHasEverBeenResidentUKPage.nextPage(
          CheckMode,
          emptyUserAnswers.set(MemberHasEverBeenResidentUKPage, true).success.value
        ) mustEqual routes.MembersLastUKAddressController.onPageLoad(CheckMode)
      }
    }

    "in FinalCheck Mode" - {

      "must go to FinalCheck Answers page when answer is 'false'" in {

        MemberHasEverBeenResidentUKPage.nextPage(
          FinalCheckMode,
          emptyUserAnswers.set(MemberHasEverBeenResidentUKPage, false).success.value
        ) mustEqual controllers.checkYourAnswers.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go to Member Last UK Address Lookup in FinalCheckMode when answer is 'true'" in {

        MemberHasEverBeenResidentUKPage.nextPage(
          FinalCheckMode,
          emptyUserAnswers.set(MemberHasEverBeenResidentUKPage, true).success.value
        ) mustEqual routes.MembersLastUKAddressController.onPageLoad(FinalCheckMode)
      }
    }

    "in AmendCheck Mode" - {

      "must go to AmendCheck Answers page when answer is 'false'" in {

        MemberHasEverBeenResidentUKPage.nextPage(
          AmendCheckMode,
          emptyUserAnswers.set(MemberHasEverBeenResidentUKPage, false).success.value
        ) mustEqual controllers.viewandamend.routes.ViewAmendSubmittedController.amend()
      }

      "must go to Member Last UK Address Lookup in AmendCheckMode when answer is 'true'" in {

        MemberHasEverBeenResidentUKPage.nextPage(
          AmendCheckMode,
          emptyUserAnswers.set(MemberHasEverBeenResidentUKPage, true).success.value
        ) mustEqual routes.MembersLastUKAddressController.onPageLoad(AmendCheckMode)
      }
    }
  }

  "cleanup" - {

    "must remove MembersLastUKAddressPage and MemberDateOfLeavingUKPage when answer is 'false'" in {
      val uaWithDeps =
        emptyUserAnswers
          .set(
            MembersLastUKAddressPage,
            MembersLastUKAddress(
              addressLine1 = "1 Test Street",
              addressLine2 = "Test District",
              addressLine3 = None,
              addressLine4 = None,
              ukPostCode   = "BB1 1BB"
            )
          ).success.value
          .set(MemberDateOfLeavingUKPage, LocalDate.of(2020, 1, 2)).success.value

      val cleaned = MemberHasEverBeenResidentUKPage.cleanup(Some(false), uaWithDeps).success.value

      cleaned.get(MembersLastUKAddressPage) mustBe None
      cleaned.get(MemberDateOfLeavingUKPage) mustBe None
    }

    "must not remove dependent pages when answer is 'true'" in {
      val uaWithDeps =
        emptyUserAnswers
          .set(
            MembersLastUKAddressPage,
            MembersLastUKAddress(
              addressLine1 = "1 Test Street",
              addressLine2 = "Test District",
              addressLine3 = None,
              addressLine4 = None,
              ukPostCode   = "BB1 1BB"
            )
          ).success.value
          .set(MemberDateOfLeavingUKPage, LocalDate.of(2020, 1, 2)).success.value

      val cleaned = MemberHasEverBeenResidentUKPage.cleanup(Some(true), uaWithDeps).success.value

      cleaned.get(MembersLastUKAddressPage).isDefined mustBe true
      cleaned.get(MemberDateOfLeavingUKPage) mustBe Some(LocalDate.of(2020, 1, 2))
    }
  }
}

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

class MemberIsResidentUKPageSpec extends AnyFreeSpec with SpecBase with Matchers {

  ".nextPage" - {

    "in Normal Mode" - {

      "must go to Check Answers page when 'true'" in {

        MemberIsResidentUKPage.nextPage(
          NormalMode,
          emptyUserAnswers.set(MemberIsResidentUKPage, true).success.value
        ) mustEqual routes.MemberDetailsCYAController.onPageLoad()
      }

      "must go to Member Has Ever Been Uk Resident when 'false'" in {

        MemberIsResidentUKPage.nextPage(
          NormalMode,
          emptyUserAnswers.set(MemberIsResidentUKPage, false).success.value
        ) mustEqual routes.MemberHasEverBeenResidentUKController.onPageLoad(NormalMode)
      }
    }

    "in Check Mode" - {

      "must go to Check Answers page when 'true'" in {

        MemberIsResidentUKPage.nextPage(
          CheckMode,
          emptyUserAnswers.set(MemberIsResidentUKPage, true).success.value
        ) mustEqual routes.MemberDetailsCYAController.onPageLoad()
      }

      "must go to Member Has Ever Been Uk Resident in check mode when 'false'" in {

        MemberIsResidentUKPage.nextPage(
          CheckMode,
          emptyUserAnswers.set(MemberIsResidentUKPage, false).success.value
        ) mustEqual routes.MemberHasEverBeenResidentUKController.onPageLoad(CheckMode)
      }
    }

    "in FinalCheck Mode" - {

      "must go to FinalCheck Answers page when 'true'" in {

        MemberIsResidentUKPage.nextPage(
          FinalCheckMode,
          emptyUserAnswers.set(MemberIsResidentUKPage, true).success.value
        ) mustEqual controllers.checkYourAnswers.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go to Member Has Ever Been Uk Resident in FinalCheck mode when 'false'" in {

        MemberIsResidentUKPage.nextPage(
          FinalCheckMode,
          emptyUserAnswers.set(MemberIsResidentUKPage, false).success.value
        ) mustEqual routes.MemberHasEverBeenResidentUKController.onPageLoad(FinalCheckMode)
      }
    }

    "in AmendCheck Mode" - {

      "must go to AmendCheck Answers page when 'true'" in {

        MemberIsResidentUKPage.nextPage(
          AmendCheckMode,
          emptyUserAnswers.set(MemberIsResidentUKPage, true).success.value
        ) mustEqual controllers.routes.ViewAmendSubmittedController.amend()
      }

      "must go to Member Has Ever Been Uk Resident in AmendCheck mode when 'false'" in {

        MemberIsResidentUKPage.nextPage(
          AmendCheckMode,
          emptyUserAnswers.set(MemberIsResidentUKPage, false).success.value
        ) mustEqual routes.MemberHasEverBeenResidentUKController.onPageLoad(AmendCheckMode)
      }
    }
  }

  "cleanup" - {

    "must remove MemberHasEverBeenResidentUKPage, MembersLastUKAddressPage and MemberDateOfLeavingUKPage when isResidentUk is true" in {
      val uaWithDeps =
        emptyUserAnswers
          .set(MemberHasEverBeenResidentUKPage, true).success.value
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

      val cleaned = MemberIsResidentUKPage.cleanup(Some(true), uaWithDeps).success.value

      cleaned.get(MemberHasEverBeenResidentUKPage) mustBe None
      cleaned.get(MembersLastUKAddressPage) mustBe None
      cleaned.get(MemberDateOfLeavingUKPage) mustBe None
    }

    "must not remove dependent pages when isResidentUk is false" in {
      val uaWithDeps =
        emptyUserAnswers
          .set(MemberHasEverBeenResidentUKPage, false).success.value
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

      val cleaned = MemberIsResidentUKPage.cleanup(Some(false), uaWithDeps).success.value

      cleaned.get(MemberHasEverBeenResidentUKPage) mustBe Some(false)
      cleaned.get(MembersLastUKAddressPage).isDefined mustBe true
      cleaned.get(MemberDateOfLeavingUKPage) mustBe Some(LocalDate.of(2020, 1, 2))
    }
  }
}

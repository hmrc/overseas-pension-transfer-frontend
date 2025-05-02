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
import models.{CheckMode, NormalMode}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

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
    }
    "in Normal Mode" - {

      "must go to Member Last UK Address Lookup when answer is 'true'" in {

        MemberHasEverBeenResidentUKPage.nextPage(
          NormalMode,
          emptyUserAnswers.set(MemberHasEverBeenResidentUKPage, true).success.value
        ) mustEqual routes.MembersLastUkAddressLookupController.onPageLoad(NormalMode)
      }
    }

    "in Check Mode" - {

      "must go to Check Answers" in {

        MemberHasEverBeenResidentUKPage.nextPage(CheckMode, emptyUserAnswers) mustEqual routes.MemberDetailsCYAController.onPageLoad()
      }
    }
  }
}

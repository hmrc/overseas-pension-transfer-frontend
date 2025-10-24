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
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class MembersLastUkAddressSelectPageSpec extends AnyFreeSpec with Matchers with SpecBase {

  ".nextPage" - {

    val emptyAnswers = UserAnswers(userAnswersTransferNumber, PstrNumber("12345678AB"))

    "in Normal Mode" - {

      "must go to confirm address page" in {

        MembersLastUkAddressSelectPage.nextPage(NormalMode, emptyAnswers) mustEqual routes.MembersLastUkAddressConfirmController.onPageLoad(
          NormalMode
        )
      }
    }

    "in Check Mode" - {

      "must go to confirm address page" in {

        MembersLastUkAddressSelectPage.nextPage(CheckMode, emptyAnswers) mustEqual routes.MembersLastUkAddressConfirmController.onPageLoad(
          CheckMode
        )
      }
    }

    "in FinalCheck Mode" - {

      "must go to confirm address page" in {

        MembersLastUkAddressSelectPage.nextPage(FinalCheckMode, emptyAnswers) mustEqual routes.MembersLastUkAddressConfirmController.onPageLoad(
          FinalCheckMode
        )
      }
    }

    "in AmendCheck Mode" - {

      "must go to confirm address page" in {

        MembersLastUkAddressSelectPage.nextPage(AmendCheckMode, emptyAnswers) mustEqual routes.MembersLastUkAddressConfirmController.onPageLoad(
          AmendCheckMode
        )
      }
    }
  }
}

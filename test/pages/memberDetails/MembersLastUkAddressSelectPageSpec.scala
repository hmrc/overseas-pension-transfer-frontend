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
import models.{CheckMode, FinalCheckMode, NormalMode, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class MembersLastUkAddressSelectPageSpec extends AnyFreeSpec with Matchers {

  ".nextPage" - {

    val emptyAnswers = UserAnswers("id")

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
          NormalMode
        )
      }
    }

    "in FinalCheckMode" - {
      "must go to Final Check Answers page" in {
        MembersLastUkAddressSelectPage.nextPage(FinalCheckMode, emptyAnswers) mustEqual
          controllers.checkYourAnswers.routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}

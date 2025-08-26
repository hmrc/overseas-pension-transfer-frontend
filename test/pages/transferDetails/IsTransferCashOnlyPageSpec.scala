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

package pages.transferDetails

import controllers.transferDetails.routes
import models.{CheckMode, FinalCheckMode, NormalMode, UserAnswers}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class IsTransferCashOnlyPageSpec extends AnyFreeSpec with Matchers {

  ".nextPage" - {

    val emptyAnswers = UserAnswers("id")

    "in Normal Mode" - {

      "must go to cya page if true is selected" in {
        val ua = emptyAnswers.set(IsTransferCashOnlyPage, true).success.value
        IsTransferCashOnlyPage.nextPage(NormalMode, ua) mustBe routes.TransferDetailsCYAController.onPageLoad()
      }

      "must go to type of asset page if false is selected" in {
        val ua = emptyAnswers.set(IsTransferCashOnlyPage, false).success.value
        IsTransferCashOnlyPage.nextPage(NormalMode, ua) mustEqual routes.TypeOfAssetController.onPageLoad(NormalMode)
      }
    }

    "in Check Mode" - {

      "must go to Check Answers" in {

        IsTransferCashOnlyPage.nextPage(CheckMode, emptyAnswers) mustEqual routes.TransferDetailsCYAController.onPageLoad()
      }
    }

    "in FinalCheckMode" - {
      "must go to Final Check Answers page" in {
        IsTransferCashOnlyPage.nextPage(FinalCheckMode, emptyAnswers) mustEqual
          controllers.checkYourAnswers.routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}

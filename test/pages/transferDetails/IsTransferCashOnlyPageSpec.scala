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
import models.{AmendCheckMode, CheckMode, FinalCheckMode, NormalMode, PstrNumber, UserAnswers}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class IsTransferCashOnlyPageSpec extends AnyFreeSpec with Matchers {

  ".nextPage" - {

    val emptyAnswers = UserAnswers("id", PstrNumber("12345678AB"))

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

      "must go to cya page if true is selected" in {
        val ua = emptyAnswers.set(IsTransferCashOnlyPage, true).success.value
        IsTransferCashOnlyPage.nextPage(CheckMode, ua) mustBe routes.TransferDetailsCYAController.onPageLoad()
      }

      "must go to type of asset page if false is selected" in {
        val ua = emptyAnswers.set(IsTransferCashOnlyPage, false).success.value
        IsTransferCashOnlyPage.nextPage(CheckMode, ua) mustEqual routes.TypeOfAssetController.onPageLoad(CheckMode)
      }
    }

    "in FinalCheckMode" - {
      "must go to final cya page if true is selected" in {
        val ua = emptyAnswers.set(IsTransferCashOnlyPage, true).success.value
        IsTransferCashOnlyPage.nextPage(FinalCheckMode, ua) mustBe controllers.checkYourAnswers.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go to type of asset page if false is selected" in {
        val ua = emptyAnswers.set(IsTransferCashOnlyPage, false).success.value
        IsTransferCashOnlyPage.nextPage(FinalCheckMode, ua) mustEqual routes.TypeOfAssetController.onPageLoad(FinalCheckMode)
      }
    }

    "in AmendCheckMode" - {
      "must go to amend cya page if true is selected" in {
        val ua = emptyAnswers.set(IsTransferCashOnlyPage, true).success.value
        IsTransferCashOnlyPage.nextPage(AmendCheckMode, ua) mustBe controllers.routes.ViewAmendSubmittedController.amend()
      }

      "must go to type of asset page if false is selected" in {
        val ua = emptyAnswers.set(IsTransferCashOnlyPage, false).success.value
        IsTransferCashOnlyPage.nextPage(AmendCheckMode, ua) mustEqual routes.TypeOfAssetController.onPageLoad(AmendCheckMode)
      }
    }
  }
}

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

import base.SpecBase
import controllers.transferDetails.routes
import models.WhyTransferIsTaxable.{NoExclusion, TransferExceedsOTCAllowance}
import models.{AmendCheckMode, CheckMode, FinalCheckMode, NormalMode, PstrNumber, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class WhyTransferIsTaxablePageSpec extends AnyFreeSpec with Matchers with SpecBase {

  ".nextPage" - {

    val emptyAnswers = UserAnswers(userAnswersTransferNumber, PstrNumber("12345678AB"))

    "in Normal Mode" - {

      "must go to applicable tax exclusion page if TransferExceedsOTCAllowance selected" in {
        val ua = emptyAnswers.set(WhyTransferIsTaxablePage, TransferExceedsOTCAllowance).success.value
        WhyTransferIsTaxablePage.nextPage(NormalMode, ua) mustEqual routes.ApplicableTaxExclusionsController.onPageLoad(NormalMode)
      }

      "must go to amount of tax deducted page if NoExclusion selected" in {
        val ua = emptyAnswers.set(WhyTransferIsTaxablePage, NoExclusion).success.value
        WhyTransferIsTaxablePage.nextPage(NormalMode, ua) mustEqual routes.AmountOfTaxDeductedController.onPageLoad(NormalMode)
      }
    }

    "in Check Mode" - {

      "must go to applicable tax exclusion page if TransferExceedsOTCAllowance selected" in {
        val ua = emptyAnswers.set(WhyTransferIsTaxablePage, TransferExceedsOTCAllowance).success.value
        WhyTransferIsTaxablePage.nextPage(CheckMode, ua) mustEqual routes.ApplicableTaxExclusionsController.onPageLoad(CheckMode)
      }

      "must go to amount of tax deducted page if NoExclusion selected" in {
        val ua = emptyAnswers.set(WhyTransferIsTaxablePage, NoExclusion).success.value
        WhyTransferIsTaxablePage.nextPage(CheckMode, ua) mustEqual routes.AmountOfTaxDeductedController.onPageLoad(CheckMode)
      }
    }

    "in FinalCheckMode" - {

      "must go to applicable tax exclusion page if TransferExceedsOTCAllowance selected" in {
        val ua = emptyAnswers.set(WhyTransferIsTaxablePage, TransferExceedsOTCAllowance).success.value
        WhyTransferIsTaxablePage.nextPage(FinalCheckMode, ua) mustEqual routes.ApplicableTaxExclusionsController.onPageLoad(FinalCheckMode)
      }

      "must go to amount of tax deducted page if NoExclusion selected" in {
        val ua = emptyAnswers.set(WhyTransferIsTaxablePage, NoExclusion).success.value
        WhyTransferIsTaxablePage.nextPage(FinalCheckMode, ua) mustEqual routes.AmountOfTaxDeductedController.onPageLoad(FinalCheckMode)
      }
    }

    "in AmendCheckMode" - {

      "must go to applicable tax exclusion page if TransferExceedsOTCAllowance selected" in {
        val ua = emptyAnswers.set(WhyTransferIsTaxablePage, TransferExceedsOTCAllowance).success.value
        WhyTransferIsTaxablePage.nextPage(AmendCheckMode, ua) mustEqual routes.ApplicableTaxExclusionsController.onPageLoad(AmendCheckMode)
      }

      "must go to amount of tax deducted page if NoExclusion selected" in {
        val ua = emptyAnswers.set(WhyTransferIsTaxablePage, NoExclusion).success.value
        WhyTransferIsTaxablePage.nextPage(AmendCheckMode, ua) mustEqual routes.AmountOfTaxDeductedController.onPageLoad(AmendCheckMode)
      }
    }
  }
}

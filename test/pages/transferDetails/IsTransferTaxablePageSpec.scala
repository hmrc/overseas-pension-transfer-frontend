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
import models.ApplicableTaxExclusions.Occupational
import models.WhyTransferIsNotTaxable.{IndividualIsEmployedPublicService, IndividualIsEmployeeOccupational}
import models.WhyTransferIsTaxable.{NoExclusion, TransferExceedsOTCAllowance}
import models.{AmendCheckMode, ApplicableTaxExclusions, CheckMode, FinalCheckMode, NormalMode, PstrNumber, UserAnswers, WhyTransferIsNotTaxable}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class IsTransferTaxablePageSpec extends AnyFreeSpec with Matchers with SpecBase {

  ".nextPage" - {

    val emptyAnswers = UserAnswers(userAnswersTransferNumber, PstrNumber("12345678AB"))

    "in Normal Mode" - {

      "must go to why transfer is taxable page if user selects true" in {
        val ua = emptyAnswers.set(IsTransferTaxablePage, true).success.value
        IsTransferTaxablePage.nextPage(NormalMode, ua) mustEqual routes.WhyTransferIsTaxableController.onPageLoad(NormalMode)
      }

      "must go to why transfer is not taxable page if user selects false" in {
        val ua = emptyAnswers.set(IsTransferTaxablePage, false).success.value
        IsTransferTaxablePage.nextPage(NormalMode, ua) mustEqual routes.WhyTransferIsNotTaxableController.onPageLoad(NormalMode)
      }
    }

    "in Check Mode" - {
      "must go to why transfer is taxable page if user selects true" in {
        val ua = emptyAnswers.set(IsTransferTaxablePage, true).success.value
        IsTransferTaxablePage.nextPage(CheckMode, ua) mustEqual routes.WhyTransferIsTaxableController.onPageLoad(CheckMode)
      }

      "must go to why transfer is not taxable page if user selects false" in {
        val ua = emptyAnswers.set(IsTransferTaxablePage, false).success.value
        IsTransferTaxablePage.nextPage(CheckMode, ua) mustEqual routes.WhyTransferIsNotTaxableController.onPageLoad(CheckMode)
      }
    }

    "in FinalCheckMode" - {
      "must go to why transfer is taxable page if user selects true" in {
        val ua = emptyAnswers.set(IsTransferTaxablePage, true).success.value
        IsTransferTaxablePage.nextPage(FinalCheckMode, ua) mustEqual routes.WhyTransferIsTaxableController.onPageLoad(FinalCheckMode)
      }

      "must go to why transfer is not taxable page if user selects false" in {
        val ua = emptyAnswers.set(IsTransferTaxablePage, false).success.value
        IsTransferTaxablePage.nextPage(FinalCheckMode, ua) mustEqual routes.WhyTransferIsNotTaxableController.onPageLoad(FinalCheckMode)
      }
    }

    "in AmendCheckMode" - {
      "must go to why transfer is taxable page if user selects true" in {
        val ua = emptyAnswers.set(IsTransferTaxablePage, true).success.value
        IsTransferTaxablePage.nextPage(AmendCheckMode, ua) mustEqual routes.WhyTransferIsTaxableController.onPageLoad(AmendCheckMode)
      }

      "must go to why transfer is not taxable page if user selects false" in {
        val ua = emptyAnswers.set(IsTransferTaxablePage, false).success.value
        IsTransferTaxablePage.nextPage(AmendCheckMode, ua) mustEqual routes.WhyTransferIsNotTaxableController.onPageLoad(AmendCheckMode)
      }
    }

  }

  "cleanup" - {
    "remove correct data when changing No to Yes" in {
      val emptyUA = UserAnswers(userAnswersTransferNumber, PstrNumber("PSTR123"))
        .set(OverseasTransferAllowancePage, BigDecimal(100)).success.value

      val result =
        IsTransferTaxablePage.cleanup(
          Some(true),
          emptyUA.set(WhyTransferIsNotTaxablePage, WhyTransferIsNotTaxable.values.toSet).success.value
        ).success.value

      result mustBe emptyUA
    }

    "remove correct data when changing Yes to No - Exceeds allowance" in {
      val emptyUA = UserAnswers(userAnswersTransferNumber, PstrNumber("PSTR123"))
        .set(OverseasTransferAllowancePage, BigDecimal(100)).success.value

      val userAnswersWithYesJourney =
        emptyUA
          .set(WhyTransferIsTaxablePage, TransferExceedsOTCAllowance).success.value
          .set(ApplicableTaxExclusionsPage, ApplicableTaxExclusions.values.toSet).success.value
          .set(AmountOfTaxDeductedPage, BigDecimal(100)).success.value
          .set(NetTransferAmountPage, BigDecimal(100)).success.value

      val result = IsTransferTaxablePage.cleanup(Some(false), userAnswersWithYesJourney).success.value

      result mustBe emptyUA
    }

    "remove correct data when changing Yes to No - No exclusions" in {
      val emptyUA = UserAnswers(userAnswersTransferNumber, PstrNumber("PSTR123"))
        .set(OverseasTransferAllowancePage, BigDecimal(100)).success.value

      val userAnswersWithYesJourney =
        emptyUA
          .set(WhyTransferIsTaxablePage, NoExclusion).success.value
          .set(AmountOfTaxDeductedPage, BigDecimal(100)).success.value
          .set(NetTransferAmountPage, BigDecimal(100)).success.value

      val result = IsTransferTaxablePage.cleanup(Some(false), userAnswersWithYesJourney).success.value

      result mustBe emptyUA
    }
  }
}

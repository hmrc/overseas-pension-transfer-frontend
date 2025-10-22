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
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class AmountOfTaxDeductedPageSpec extends AnyFreeSpec with Matchers {

  ".nextPage" - {

    val emptyAnswers = UserAnswers("id", PstrNumber("12345678AB"))

    "in Normal Mode" - {

      "must go to the Next page" in {
        AmountOfTaxDeductedPage.nextPage(NormalMode, emptyAnswers) mustEqual routes.NetTransferAmountController.onPageLoad(NormalMode)
      }
    }

    "in Check Mode" - {

      "must go to Check Net Transfer amount in checkMode" in {

        AmountOfTaxDeductedPage.nextPage(CheckMode, emptyAnswers) mustEqual routes.NetTransferAmountController.onPageLoad(CheckMode)
      }
    }

    "in FinalCheckMode" - {
      "must go to Check Net Transfer amount in FinalCheckMode" in {

        AmountOfTaxDeductedPage.nextPage(FinalCheckMode, emptyAnswers) mustEqual routes.NetTransferAmountController.onPageLoad(FinalCheckMode)
      }
    }

    "in AmendCheckMode" - {
      "must go to Check Net Transfer amount in AmendCheckMode" in {
        AmountOfTaxDeductedPage.nextPage(AmendCheckMode, emptyAnswers) mustEqual routes.NetTransferAmountController.onPageLoad(AmendCheckMode)
      }
    }
  }
}

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

package pages.transferDetails.assetsMiniJourneys.unquotedShares

import base.SpecBase
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import models.{AmendCheckMode, CheckMode, FinalCheckMode, NormalMode, PstrNumber, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.transferDetails.assetsMiniJourneys.quotedShares.QuotedSharesCompanyNamePage

class UnquotedSharesCompanyNamePageSpec extends AnyFreeSpec with Matchers with SpecBase {
  private val index = 0

  ".nextPage" - {

    val emptyAnswers = UserAnswers(userAnswersTransferNumber, PstrNumber("12345678AB"))

    "in Normal Mode" - {
      "must go to next page" in {
        UnquotedSharesCompanyNamePage(index).nextPage(NormalMode, emptyAnswers) mustEqual AssetsMiniJourneysRoutes.UnquotedSharesValueController.onPageLoad(
          NormalMode,
          index
        )
      }
    }

    "in Check Mode" - {
      "must go to next page" in {
        UnquotedSharesCompanyNamePage(index).nextPage(CheckMode, emptyAnswers) mustEqual AssetsMiniJourneysRoutes.UnquotedSharesValueController.onPageLoad(
          CheckMode,
          index
        )
      }
    }

    "in FinalCheckMode" - {
      "must go to next page" in {
        UnquotedSharesCompanyNamePage(index).nextPage(FinalCheckMode, emptyAnswers) mustEqual AssetsMiniJourneysRoutes.UnquotedSharesValueController.onPageLoad(
          FinalCheckMode,
          index
        )
      }
    }

    "in AmendCheckMode" - {
      "must go to next page" in {
        UnquotedSharesCompanyNamePage(index).nextPage(AmendCheckMode, emptyAnswers) mustEqual AssetsMiniJourneysRoutes.UnquotedSharesValueController.onPageLoad(
          AmendCheckMode,
          index
        )
      }
    }
  }
}

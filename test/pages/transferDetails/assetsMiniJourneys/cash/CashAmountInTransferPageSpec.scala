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

package pages.transferDetails.assetsMiniJourneys.cash

import base.SpecBase
import controllers.transferDetails.routes
import models.assets.TypeOfAsset.Cash
import models.assets.{CashMiniJourney, QuotedSharesMiniJourney, TypeOfAsset}
import models.{AmendCheckMode, CheckMode, FinalCheckMode, NormalMode, PstrNumber, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import pages.transferDetails.TypeOfAssetPage
import queries.assets.AssetCompletionFlag

class CashAmountInTransferPageSpec extends AnyFreeSpec with SpecBase {

  ".nextPage" - {

    val emptyAnswers = UserAnswers(userAnswersTransferNumber, PstrNumber("12345678AB"))

    "in Normal Mode" - {
      "must go to the cya page if no more assets" in {
        CashAmountInTransferPage.nextPageWith(NormalMode, emptyAnswers, emptySessionData) mustEqual routes.TransferDetailsCYAController.onPageLoad()
      }

      "must go to the next asset page if more assets" in {
        val selectedTypes: Seq[TypeOfAsset] = Seq(CashMiniJourney.assetType, QuotedSharesMiniJourney.assetType)
        val sessionData                     = for {
          sd1 <- emptySessionData.set(TypeOfAssetPage, selectedTypes)
          sd2 <- sd1.set(AssetCompletionFlag(Cash), true)
        } yield sd2

        val result = CashAmountInTransferPage.nextPageWith(NormalMode, emptyAnswers, sessionData.success.value)
        result mustBe QuotedSharesMiniJourney.call
      }
    }

    "in Check Mode" - {
      "must go to the cya page if no more assets" in {
        CashAmountInTransferPage.nextPageWith(CheckMode, emptyAnswers, emptySessionData) mustEqual routes.TransferDetailsCYAController.onPageLoad()
      }

      "must go to the next asset page if more assets" in {
        val selectedTypes: Seq[TypeOfAsset] = Seq(CashMiniJourney.assetType, QuotedSharesMiniJourney.assetType)
        val sessionData                     = for {
          sd1 <- emptySessionData.set(TypeOfAssetPage, selectedTypes)
          sd2 <- sd1.set(AssetCompletionFlag(Cash), true)
        } yield sd2

        val result = CashAmountInTransferPage.nextPageWith(CheckMode, emptyAnswers, sessionData.success.value)
        result mustBe QuotedSharesMiniJourney.call
      }
    }

    "in Final Check Mode" - {
      "must go to the cya page if no more assets" in {
        CashAmountInTransferPage.nextPageWith(
          FinalCheckMode,
          emptyAnswers,
          emptySessionData
        ) mustEqual controllers.checkYourAnswers.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go to the next asset page if more assets" in {
        val selectedTypes: Seq[TypeOfAsset] = Seq(CashMiniJourney.assetType, QuotedSharesMiniJourney.assetType)
        val sessionData                     = for {
          sd1 <- emptySessionData.set(TypeOfAssetPage, selectedTypes)
          sd2 <- sd1.set(AssetCompletionFlag(Cash), true)
        } yield sd2

        val result = CashAmountInTransferPage.nextPageWith(FinalCheckMode, emptyAnswers, sessionData.success.value)
        result mustBe QuotedSharesMiniJourney.call
      }
    }

    "in Amend Check Mode" - {
      "must go to the cya page if no more assets" in {
        CashAmountInTransferPage.nextPageWith(
          AmendCheckMode,
          emptyAnswers,
          emptySessionData
        ) mustEqual controllers.viewandamend.routes.ViewAmendSubmittedController.amend()
      }

      "must go to the next asset page if more assets" in {
        val selectedTypes: Seq[TypeOfAsset] = Seq(CashMiniJourney.assetType, QuotedSharesMiniJourney.assetType)
        val sessionData                     = for {
          sd1 <- emptySessionData.set(TypeOfAssetPage, selectedTypes)
          sd2 <- sd1.set(AssetCompletionFlag(Cash), true)
        } yield sd2

        val result = CashAmountInTransferPage.nextPageWith(AmendCheckMode, emptyAnswers, sessionData.success.value)
        result mustBe QuotedSharesMiniJourney.call
      }
    }
  }
}

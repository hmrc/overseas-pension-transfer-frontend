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

package pages.transferDetails.assetsMiniJourneys.quotedShares

import base.SpecBase
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import controllers.transferDetails.routes
import models.assets.{PropertyMiniJourney, QuotedSharesMiniJourney, TypeOfAsset, UnquotedSharesMiniJourney}
import models.{AmendCheckMode, CheckMode, FinalCheckMode, NormalMode, PstrNumber, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import pages.transferDetails.TypeOfAssetPage
import pages.transferDetails.assetsMiniJourneys.property.PropertyAmendContinuePage
import queries.assets.AssetCompletionFlag

class QuotedSharesAmendContinuePageSpec extends AnyFreeSpec with SpecBase {

  ".nextPage" - {

    val emptyAnswers = UserAnswers(userAnswersTransferNumber, PstrNumber("12345678AB"))

    "in Normal Mode" - {

      "must go to the first page in mini journey if continue selected" in {
        val userAnswers = emptyAnswers.set(QuotedSharesAmendContinuePage, true).success.value
        val nextIndex   = 1
        QuotedSharesAmendContinuePage.nextPageWith(
          NormalMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual AssetsMiniJourneysRoutes.QuotedSharesCompanyNameController.onPageLoad(
          NormalMode,
          nextIndex
        )
      }

      "must go to the cya page if no-continue selected and no more assets" in {
        val userAnswers = emptyAnswers.set(QuotedSharesAmendContinuePage, false).success.value
        val nextIndex   = 2
        QuotedSharesAmendContinuePage.nextPageWith(
          NormalMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual routes.TransferDetailsCYAController.onPageLoad()
      }

      "must go to the next asset page if no-continue selected and more assets" in {
        val selectedTypes: Seq[TypeOfAsset] = Seq(QuotedSharesMiniJourney.assetType, UnquotedSharesMiniJourney.assetType)
        val userAnswers                     = emptyAnswers.set(QuotedSharesAmendContinuePage, false)
        val sessionData                     =
          for {
            sd1 <- emptySessionData.set(TypeOfAssetPage, selectedTypes)
            sd2 <- sd1.set(AssetCompletionFlag(TypeOfAsset.QuotedShares), true)
          } yield sd2

        val result = QuotedSharesAmendContinuePage.nextPageWith(NormalMode, userAnswers.success.value, (sessionData.success.value, 0))
        result mustBe UnquotedSharesMiniJourney.call
      }
    }

    "in Check Mode" - {

      "must go to the first page in mini journey if continue selected" in {
        val userAnswers = emptyAnswers.set(QuotedSharesAmendContinuePage, true).success.value
        val nextIndex   = 1
        QuotedSharesAmendContinuePage.nextPageWith(
          CheckMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual AssetsMiniJourneysRoutes.QuotedSharesCompanyNameController.onPageLoad(
          CheckMode,
          nextIndex
        )
      }

      "must go to the cya page if no-continue selected and no more assets" in {
        val userAnswers = emptyAnswers.set(QuotedSharesAmendContinuePage, false).success.value
        val nextIndex   = 2
        QuotedSharesAmendContinuePage.nextPageWith(
          CheckMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual routes.TransferDetailsCYAController.onPageLoad()
      }

      "must go to the next asset page if no-continue selected and more assets" in {
        val selectedTypes: Seq[TypeOfAsset] = Seq(QuotedSharesMiniJourney.assetType, UnquotedSharesMiniJourney.assetType)
        val userAnswers                     = emptyAnswers.set(QuotedSharesAmendContinuePage, false)
        val sessionData                     =
          for {
            sd1 <- emptySessionData.set(TypeOfAssetPage, selectedTypes)
            sd2 <- sd1.set(AssetCompletionFlag(TypeOfAsset.QuotedShares), true)
          } yield sd2

        val result = QuotedSharesAmendContinuePage.nextPageWith(CheckMode, userAnswers.success.value, (sessionData.success.value, 0))
        result mustBe UnquotedSharesMiniJourney.call
      }
    }

    "in Final Check Mode" - {

      "must go to the first page in mini journey if continue selected" in {
        val userAnswers = emptyAnswers.set(QuotedSharesAmendContinuePage, true).success.value
        val nextIndex   = 1
        QuotedSharesAmendContinuePage.nextPageWith(
          FinalCheckMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual AssetsMiniJourneysRoutes.QuotedSharesCompanyNameController.onPageLoad(
          FinalCheckMode,
          nextIndex
        )
      }

      "must go to the cya page if no-continue selected and no more assets" in {
        val userAnswers = emptyAnswers.set(QuotedSharesAmendContinuePage, false).success.value
        val nextIndex   = 2
        QuotedSharesAmendContinuePage.nextPageWith(
          FinalCheckMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual controllers.checkYourAnswers.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go to the next asset page if no-continue selected and more assets" in {
        val selectedTypes: Seq[TypeOfAsset] = Seq(QuotedSharesMiniJourney.assetType, UnquotedSharesMiniJourney.assetType)
        val userAnswers                     = emptyAnswers.set(QuotedSharesAmendContinuePage, false)
        val sessionData                     =
          for {
            sd1 <- emptySessionData.set(TypeOfAssetPage, selectedTypes)
            sd2 <- sd1.set(AssetCompletionFlag(TypeOfAsset.QuotedShares), true)
          } yield sd2

        val result = QuotedSharesAmendContinuePage.nextPageWith(FinalCheckMode, userAnswers.success.value, (sessionData.success.value, 0))
        result mustBe UnquotedSharesMiniJourney.call
      }
    }

    "in Amend Check Mode" - {

      "must go to the first page in mini journey if continue selected" in {
        val userAnswers = emptyAnswers.set(QuotedSharesAmendContinuePage, true).success.value
        val nextIndex   = 1
        QuotedSharesAmendContinuePage.nextPageWith(
          AmendCheckMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual AssetsMiniJourneysRoutes.QuotedSharesCompanyNameController.onPageLoad(
          AmendCheckMode,
          nextIndex
        )
      }

      "must go to the cya page if no-continue selected and no more assets" in {
        val userAnswers = emptyAnswers.set(QuotedSharesAmendContinuePage, false).success.value
        val nextIndex   = 2
        QuotedSharesAmendContinuePage.nextPageWith(
          AmendCheckMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual controllers.viewandamend.routes.ViewAmendSubmittedController.amend()
      }

      "must go to the next asset page if no-continue selected and more assets" in {
        val selectedTypes: Seq[TypeOfAsset] = Seq(QuotedSharesMiniJourney.assetType, UnquotedSharesMiniJourney.assetType)
        val userAnswers                     = emptyAnswers.set(QuotedSharesAmendContinuePage, false)
        val sessionData                     =
          for {
            sd1 <- emptySessionData.set(TypeOfAssetPage, selectedTypes)
            sd2 <- sd1.set(AssetCompletionFlag(TypeOfAsset.QuotedShares), true)
          } yield sd2

        val result = QuotedSharesAmendContinuePage.nextPageWith(AmendCheckMode, userAnswers.success.value, (sessionData.success.value, 0))
        result mustBe UnquotedSharesMiniJourney.call
      }
    }
  }
}

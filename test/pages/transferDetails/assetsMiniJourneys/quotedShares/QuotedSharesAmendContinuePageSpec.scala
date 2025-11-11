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
import models.assets.{QuotedSharesMiniJourney, UnquotedSharesMiniJourney}
import models.{AmendCheckMode, CheckMode, FinalCheckMode, NormalMode, PstrNumber, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import queries.assets.{SelectedAssetTypesWithStatus, SessionAssetTypeWithStatus}

class QuotedSharesAmendContinuePageSpec extends AnyFreeSpec with SpecBase {

  ".nextPage" - {

    val emptyAnswers = UserAnswers(userAnswersTransferNumber, PstrNumber("12345678AB"))

    "in Normal Mode" - {

      "must go to the first page in mini journey if continue selected" in {
        val userAnswers = emptyAnswers.set(QuotedSharesAmendContinueAssetPage, true).success.value
        val nextIndex   = 1
        QuotedSharesAmendContinueAssetPage.nextPageWith(
          NormalMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual AssetsMiniJourneysRoutes.QuotedSharesStartController.onPageLoad()
      }

      "must go to the cya page if no-continue selected and no more assets" in {
        val userAnswers = emptyAnswers.set(QuotedSharesAmendContinueAssetPage, false).success.value
        val nextIndex   = 2
        QuotedSharesAmendContinueAssetPage.nextPageWith(
          NormalMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual routes.TransferDetailsCYAController.onPageLoad()
      }

      "must go to the next asset page if continue selected" in {
        val userAnswers = emptyAnswers.set(QuotedSharesAmendContinueAssetPage, false)
        val sessionData =
          emptySessionData.set(
            SelectedAssetTypesWithStatus,
            Seq(
              SessionAssetTypeWithStatus(QuotedSharesMiniJourney.assetType, isCompleted = true),
              SessionAssetTypeWithStatus(UnquotedSharesMiniJourney.assetType)
            )
          )

        val result = QuotedSharesAmendContinueAssetPage.nextPageWith(NormalMode, userAnswers.success.value, (sessionData.success.value, 0))
        result mustBe UnquotedSharesMiniJourney.call(NormalMode)
      }
    }

    "in Check Mode" - {

      "must go to the first page in mini journey if continue selected" in {
        val userAnswers = emptyAnswers.set(QuotedSharesAmendContinueAssetPage, true).success.value
        val nextIndex   = 1
        QuotedSharesAmendContinueAssetPage.nextPageWith(
          CheckMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual AssetsMiniJourneysRoutes.QuotedSharesCompanyNameController.onPageLoad(
          CheckMode,
          nextIndex
        )
      }

      "must go to the cya page if no-continue selected and no more assets" in {
        val userAnswers = emptyAnswers.set(QuotedSharesAmendContinueAssetPage, false).success.value
        val nextIndex   = 2
        QuotedSharesAmendContinueAssetPage.nextPageWith(
          CheckMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual routes.TransferDetailsCYAController.onPageLoad()
      }

      "must go to the next asset page if continue selected" in {
        val userAnswers = emptyAnswers.set(QuotedSharesAmendContinueAssetPage, false)
        val sessionData =
          emptySessionData.set(
            SelectedAssetTypesWithStatus,
            Seq(
              SessionAssetTypeWithStatus(QuotedSharesMiniJourney.assetType, isCompleted = true),
              SessionAssetTypeWithStatus(UnquotedSharesMiniJourney.assetType)
            )
          )

        val result = QuotedSharesAmendContinueAssetPage.nextPageWith(CheckMode, userAnswers.success.value, (sessionData.success.value, 0))
        result mustBe UnquotedSharesMiniJourney.call(CheckMode)
      }
    }

    "in Final Check Mode" - {

      "must go to the first page in mini journey if continue selected" in {
        val userAnswers = emptyAnswers.set(QuotedSharesAmendContinueAssetPage, true).success.value
        val nextIndex   = 1
        QuotedSharesAmendContinueAssetPage.nextPageWith(
          FinalCheckMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual AssetsMiniJourneysRoutes.QuotedSharesCompanyNameController.onPageLoad(
          FinalCheckMode,
          nextIndex
        )
      }

      "must go to the cya page if no-continue selected and no more assets" in {
        val userAnswers = emptyAnswers.set(QuotedSharesAmendContinueAssetPage, false).success.value
        val nextIndex   = 2
        QuotedSharesAmendContinueAssetPage.nextPageWith(
          FinalCheckMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual controllers.checkYourAnswers.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go to the next asset page if continue selected" in {
        val userAnswers = emptyAnswers.set(QuotedSharesAmendContinueAssetPage, false)
        val sessionData =
          emptySessionData.set(
            SelectedAssetTypesWithStatus,
            Seq(
              SessionAssetTypeWithStatus(QuotedSharesMiniJourney.assetType, isCompleted = true),
              SessionAssetTypeWithStatus(UnquotedSharesMiniJourney.assetType)
            )
          )

        val result = QuotedSharesAmendContinueAssetPage.nextPageWith(FinalCheckMode, userAnswers.success.value, (sessionData.success.value, 0))
        result mustBe UnquotedSharesMiniJourney.call(FinalCheckMode)
      }
    }

    "in Amend Check Mode" - {

      "must go to the first page in mini journey if continue selected" in {
        val userAnswers = emptyAnswers.set(QuotedSharesAmendContinueAssetPage, true).success.value
        val nextIndex   = 1
        QuotedSharesAmendContinueAssetPage.nextPageWith(
          AmendCheckMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual AssetsMiniJourneysRoutes.QuotedSharesCompanyNameController.onPageLoad(
          AmendCheckMode,
          nextIndex
        )
      }

      "must go to the cya page if no-continue selected and no more assets" in {
        val userAnswers = emptyAnswers.set(QuotedSharesAmendContinueAssetPage, false).success.value
        val nextIndex   = 2
        QuotedSharesAmendContinueAssetPage.nextPageWith(
          AmendCheckMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual controllers.viewandamend.routes.ViewAmendSubmittedController.amend()
      }

      "must go to the next asset page if continue selected" in {
        val userAnswers = emptyAnswers.set(QuotedSharesAmendContinueAssetPage, false)
        val sessionData =
          emptySessionData.set(
            SelectedAssetTypesWithStatus,
            Seq(
              SessionAssetTypeWithStatus(QuotedSharesMiniJourney.assetType, isCompleted = true),
              SessionAssetTypeWithStatus(UnquotedSharesMiniJourney.assetType)
            )
          )

        val result = QuotedSharesAmendContinueAssetPage.nextPageWith(AmendCheckMode, userAnswers.success.value, (sessionData.success.value, 0))
        result mustBe UnquotedSharesMiniJourney.call(AmendCheckMode)
      }
    }
  }
}

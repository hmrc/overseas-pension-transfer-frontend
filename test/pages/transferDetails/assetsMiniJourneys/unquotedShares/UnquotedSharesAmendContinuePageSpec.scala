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
import controllers.transferDetails.routes
import models.assets.{QuotedSharesMiniJourney, UnquotedSharesMiniJourney}
import models.{AmendCheckMode, CheckMode, FinalCheckMode, NormalMode, PstrNumber, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import queries.assets.{SelectedAssetTypesWithStatus, SessionAssetTypeWithStatus}

class UnquotedSharesAmendContinuePageSpec extends AnyFreeSpec with SpecBase {

  ".nextPageWith" - {

    val emptyAnswers = UserAnswers(userAnswersTransferNumber, PstrNumber("12345678AB"))

    "in Normal Mode" - {

      "must go to the first page in mini journey if continue selected" in {
        val userAnswers = emptyAnswers.set(UnquotedSharesAmendContinueAssetPage, true).success.value
        val nextIndex   = 1

        UnquotedSharesAmendContinueAssetPage.nextPageWith(
          NormalMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual AssetsMiniJourneysRoutes.UnquotedSharesStartController.onPageLoad()
      }

      "must go to the CYA page if no-continue selected and no more assets" in {
        val userAnswers = emptyAnswers.set(UnquotedSharesAmendContinueAssetPage, false).success.value
        val nextIndex   = 2

        UnquotedSharesAmendContinueAssetPage.nextPageWith(
          NormalMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual routes.TransferDetailsCYAController.onPageLoad()
      }

      "must go to the next asset page if no-continue selected and more assets" in {
        val userAnswers = emptyAnswers.set(UnquotedSharesAmendContinueAssetPage, false)

        val sessionData =
          emptySessionData.set(
            SelectedAssetTypesWithStatus,
            Seq(
              SessionAssetTypeWithStatus(UnquotedSharesMiniJourney.assetType, isCompleted = true),
              SessionAssetTypeWithStatus(QuotedSharesMiniJourney.assetType)
            )
          )

        val result =
          UnquotedSharesAmendContinueAssetPage.nextPageWith(
            NormalMode,
            userAnswers.success.value,
            (sessionData.success.value, 0)
          )

        result mustBe QuotedSharesMiniJourney.call(NormalMode)
      }
    }

    "in Check Mode" - {

      "must go to the first page in mini journey if continue selected" in {
        val userAnswers = emptyAnswers.set(UnquotedSharesAmendContinueAssetPage, true).success.value
        val nextIndex   = 1

        UnquotedSharesAmendContinueAssetPage.nextPageWith(
          CheckMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual AssetsMiniJourneysRoutes.UnquotedSharesCompanyNameController.onPageLoad(
          CheckMode,
          nextIndex
        )
      }

      "must go to the CYA page if no-continue selected and no more assets" in {
        val userAnswers = emptyAnswers.set(UnquotedSharesAmendContinueAssetPage, false).success.value
        val nextIndex   = 2

        UnquotedSharesAmendContinueAssetPage.nextPageWith(
          CheckMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual routes.TransferDetailsCYAController.onPageLoad()
      }

      "must go to the next asset page if no-continue selected and more assets" in {
        val userAnswers = emptyAnswers.set(UnquotedSharesAmendContinueAssetPage, false)

        val sessionData =
          emptySessionData.set(
            SelectedAssetTypesWithStatus,
            Seq(
              SessionAssetTypeWithStatus(UnquotedSharesMiniJourney.assetType, isCompleted = true),
              SessionAssetTypeWithStatus(QuotedSharesMiniJourney.assetType)
            )
          )

        val result =
          UnquotedSharesAmendContinueAssetPage.nextPageWith(
            CheckMode,
            userAnswers.success.value,
            (sessionData.success.value, 0)
          )

        result mustBe QuotedSharesMiniJourney.call(CheckMode)
      }
    }

    "in Final Check Mode" - {

      "must go to the first page in mini journey if continue selected" in {
        val userAnswers = emptyAnswers.set(UnquotedSharesAmendContinueAssetPage, true).success.value
        val nextIndex   = 1

        UnquotedSharesAmendContinueAssetPage.nextPageWith(
          FinalCheckMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual AssetsMiniJourneysRoutes.UnquotedSharesCompanyNameController.onPageLoad(
          FinalCheckMode,
          nextIndex
        )
      }

      "must go to the CYA page if no-continue selected and no more assets" in {
        val userAnswers = emptyAnswers.set(UnquotedSharesAmendContinueAssetPage, false).success.value
        val nextIndex   = 2

        UnquotedSharesAmendContinueAssetPage.nextPageWith(
          FinalCheckMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual controllers.checkYourAnswers.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go to the next asset page if no-continue selected and more assets" in {
        val userAnswers = emptyAnswers.set(UnquotedSharesAmendContinueAssetPage, false)

        val sessionData =
          emptySessionData.set(
            SelectedAssetTypesWithStatus,
            Seq(
              SessionAssetTypeWithStatus(UnquotedSharesMiniJourney.assetType, isCompleted = true),
              SessionAssetTypeWithStatus(QuotedSharesMiniJourney.assetType)
            )
          )

        val result =
          UnquotedSharesAmendContinueAssetPage.nextPageWith(
            FinalCheckMode,
            userAnswers.success.value,
            (sessionData.success.value, 0)
          )

        result mustBe QuotedSharesMiniJourney.call(FinalCheckMode)
      }
    }

    "in Amend Check Mode" - {

      "must go to the first page in mini journey if continue selected" in {
        val userAnswers = emptyAnswers.set(UnquotedSharesAmendContinueAssetPage, true).success.value
        val nextIndex   = 1

        UnquotedSharesAmendContinueAssetPage.nextPageWith(
          AmendCheckMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual AssetsMiniJourneysRoutes.UnquotedSharesCompanyNameController.onPageLoad(
          AmendCheckMode,
          nextIndex
        )
      }

      "must go to the View & Amend CYA page if no-continue selected and no more assets" in {
        val userAnswers = emptyAnswers.set(UnquotedSharesAmendContinueAssetPage, false).success.value
        val nextIndex   = 2

        UnquotedSharesAmendContinueAssetPage.nextPageWith(
          AmendCheckMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual controllers.viewandamend.routes.ViewAmendSubmittedController.amend()
      }

      "must go to the next asset page if no-continue selected and more assets" in {
        val userAnswers = emptyAnswers.set(UnquotedSharesAmendContinueAssetPage, false)

        val sessionData =
          emptySessionData.set(
            SelectedAssetTypesWithStatus,
            Seq(
              SessionAssetTypeWithStatus(UnquotedSharesMiniJourney.assetType, isCompleted = true),
              SessionAssetTypeWithStatus(QuotedSharesMiniJourney.assetType)
            )
          )

        val result =
          UnquotedSharesAmendContinueAssetPage.nextPageWith(
            AmendCheckMode,
            userAnswers.success.value,
            (sessionData.success.value, 0)
          )

        result mustBe QuotedSharesMiniJourney.call(AmendCheckMode)
      }
    }
  }
}

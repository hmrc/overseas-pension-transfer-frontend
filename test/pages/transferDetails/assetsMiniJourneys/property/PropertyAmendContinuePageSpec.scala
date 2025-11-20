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

package pages.transferDetails.assetsMiniJourneys.property

import base.SpecBase
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import controllers.transferDetails.routes
import models.assets.{PropertyMiniJourney, UnquotedSharesMiniJourney}
import models.{AmendCheckMode, CheckMode, FinalCheckMode, NormalMode, PstrNumber, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import queries.assets.{SelectedAssetTypesWithStatus, SessionAssetTypeWithStatus}

class PropertyAmendContinuePageSpec extends AnyFreeSpec with SpecBase {

  ".nextPage" - {

    val emptyAnswers = UserAnswers(userAnswersTransferNumber, PstrNumber("12345678AB"))

    "in NormalMode" - {

      "must go to the start page in mini journey if continue selected and index 0" in {
        val userAnswers = emptyAnswers.set(PropertyAmendContinueAssetPage, true).success.value
        val nextIndex   = 0
        PropertyAmendContinueAssetPage.nextPageWith(
          NormalMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual AssetsMiniJourneysRoutes.PropertyStartController.onPageLoad(NormalMode)
      }

      "must go to the address page in mini journey if continue selected and index 1" in {
        val userAnswers = emptyAnswers.set(PropertyAmendContinueAssetPage, true).success.value
        val nextIndex   = 1
        PropertyAmendContinueAssetPage.nextPageWith(
          NormalMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual AssetsMiniJourneysRoutes.PropertyAddressController.onPageLoad(NormalMode, nextIndex)
      }

      "must go to the cya page if no-continue selected and no more assets" in {
        val userAnswers = emptyAnswers.set(PropertyAmendContinueAssetPage, false).success.value
        val nextIndex   = 2
        PropertyAmendContinueAssetPage.nextPageWith(
          NormalMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual routes.TransferDetailsCYAController.onPageLoad()
      }

      "must go to the next asset page if no-continue selected and more assets" in {
        val userAnswers = emptyAnswers.set(PropertyAmendContinueAssetPage, false)
        val sessionData =
          emptySessionData.set(
            SelectedAssetTypesWithStatus,
            Seq(
              SessionAssetTypeWithStatus(PropertyMiniJourney.assetType, isCompleted = true),
              SessionAssetTypeWithStatus(UnquotedSharesMiniJourney.assetType)
            )
          )

        val result = PropertyAmendContinueAssetPage.nextPageWith(NormalMode, userAnswers.success.value, (sessionData.success.value, 0))
        result mustBe UnquotedSharesMiniJourney.call(NormalMode)
      }
    }

    "in CheckMode" - {

      "must go to the start page in mini journey if continue selected and index 0" in {
        val userAnswers = emptyAnswers.set(PropertyAmendContinueAssetPage, true).success.value
        val nextIndex   = 0
        PropertyAmendContinueAssetPage.nextPageWith(
          CheckMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual AssetsMiniJourneysRoutes.PropertyStartController.onPageLoad(CheckMode)
      }

      "must go to the address page in mini journey if continue selected and index 1" in {
        val userAnswers = emptyAnswers.set(PropertyAmendContinueAssetPage, true).success.value
        val nextIndex   = 1
        PropertyAmendContinueAssetPage.nextPageWith(
          CheckMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual AssetsMiniJourneysRoutes.PropertyAddressController.onPageLoad(CheckMode, nextIndex)
      }

      "must go to the cya page if no-continue selected and no more assets" in {
        val userAnswers = emptyAnswers.set(PropertyAmendContinueAssetPage, false).success.value
        val nextIndex   = 2
        PropertyAmendContinueAssetPage.nextPageWith(
          CheckMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual routes.TransferDetailsCYAController.onPageLoad()
      }

      "must go to the next asset page if no-continue selected and more assets" in {
        val userAnswers = emptyAnswers.set(PropertyAmendContinueAssetPage, false)
        val sessionData =
          emptySessionData.set(
            SelectedAssetTypesWithStatus,
            Seq(
              SessionAssetTypeWithStatus(PropertyMiniJourney.assetType, isCompleted = true),
              SessionAssetTypeWithStatus(UnquotedSharesMiniJourney.assetType)
            )
          )

        val result = PropertyAmendContinueAssetPage.nextPageWith(CheckMode, userAnswers.success.value, (sessionData.success.value, 0))
        result mustBe UnquotedSharesMiniJourney.call(CheckMode)
      }
    }

    "in FinalCheckMode" - {

      "must go to the start page in mini journey if continue selected and index 0" in {
        val userAnswers = emptyAnswers.set(PropertyAmendContinueAssetPage, true).success.value
        val nextIndex   = 0
        PropertyAmendContinueAssetPage.nextPageWith(
          FinalCheckMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual AssetsMiniJourneysRoutes.PropertyStartController.onPageLoad(FinalCheckMode)
      }

      "must go to the address page in mini journey if continue selected and index 1" in {
        val userAnswers = emptyAnswers.set(PropertyAmendContinueAssetPage, true).success.value
        val nextIndex   = 1
        PropertyAmendContinueAssetPage.nextPageWith(
          FinalCheckMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual AssetsMiniJourneysRoutes.PropertyAddressController.onPageLoad(FinalCheckMode, nextIndex)
      }

      "must go to the cya page if no-continue selected and no more assets" in {
        val userAnswers = emptyAnswers.set(PropertyAmendContinueAssetPage, false).success.value
        val nextIndex   = 2
        PropertyAmendContinueAssetPage.nextPageWith(
          FinalCheckMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual controllers.checkYourAnswers.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go to the next asset page if no-continue selected and more assets" in {
        val userAnswers = emptyAnswers.set(PropertyAmendContinueAssetPage, false)
        val sessionData =
          emptySessionData.set(
            SelectedAssetTypesWithStatus,
            Seq(
              SessionAssetTypeWithStatus(PropertyMiniJourney.assetType, isCompleted = true),
              SessionAssetTypeWithStatus(UnquotedSharesMiniJourney.assetType)
            )
          )

        val result = PropertyAmendContinueAssetPage.nextPageWith(FinalCheckMode, userAnswers.success.value, (sessionData.success.value, 0))
        result mustBe UnquotedSharesMiniJourney.call(FinalCheckMode)
      }
    }

    "in AmendCheckMode" - {

      "must go to the start page in mini journey if continue selected and index 0" in {
        val userAnswers = emptyAnswers.set(PropertyAmendContinueAssetPage, true).success.value
        val nextIndex   = 0
        PropertyAmendContinueAssetPage.nextPageWith(
          AmendCheckMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual AssetsMiniJourneysRoutes.PropertyStartController.onPageLoad(AmendCheckMode)
      }

      "must go to the address page in mini journey if continue selected and index 1" in {
        val userAnswers = emptyAnswers.set(PropertyAmendContinueAssetPage, true).success.value
        val nextIndex   = 1
        PropertyAmendContinueAssetPage.nextPageWith(
          AmendCheckMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual AssetsMiniJourneysRoutes.PropertyAddressController.onPageLoad(AmendCheckMode, nextIndex)
      }

      "must go to the cya page if no-continue selected and no more assets" in {
        val userAnswers = emptyAnswers.set(PropertyAmendContinueAssetPage, false).success.value
        val nextIndex   = 2
        PropertyAmendContinueAssetPage.nextPageWith(
          AmendCheckMode,
          userAnswers,
          (emptySessionData, nextIndex)
        ) mustEqual controllers.viewandamend.routes.ViewAmendSubmittedController.amend()
      }

      "must go to the next asset page if no-continue selected and more assets" in {
        val userAnswers = emptyAnswers.set(PropertyAmendContinueAssetPage, false)
        val sessionData =
          emptySessionData.set(
            SelectedAssetTypesWithStatus,
            Seq(
              SessionAssetTypeWithStatus(PropertyMiniJourney.assetType, isCompleted = true),
              SessionAssetTypeWithStatus(UnquotedSharesMiniJourney.assetType)
            )
          )

        val result = PropertyAmendContinueAssetPage.nextPageWith(AmendCheckMode, userAnswers.success.value, (sessionData.success.value, 0))
        result mustBe UnquotedSharesMiniJourney.call(AmendCheckMode)
      }
    }
  }
}

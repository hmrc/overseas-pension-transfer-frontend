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

package pages.transferDetails.assetsMiniJourneys.otherAssets

import base.SpecBase
import controllers.transferDetails.routes
import models.assets.{OtherAssetsMiniJourney, QuotedSharesMiniJourney, UnquotedSharesMiniJourney}
import models.{AmendCheckMode, CheckMode, FinalCheckMode, NormalMode, PstrNumber, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import queries.assets.{SelectedAssetTypesWithStatus, SessionAssetTypeWithStatus}

class MoreOtherAssetsDeclarationPageSpec extends AnyFreeSpec with Matchers with SpecBase {

  ".nextPage" - {

    val emptyAnswers = UserAnswers(userAnswersTransferNumber, PstrNumber("12345678AB"))

    "in Normal Mode" - {
      "must go to TransferDetailsCYAController" in {
        MoreOtherAssetsDeclarationPage.nextPageWith(NormalMode, emptyAnswers, sessionDataMemberName) mustEqual
          routes.TransferDetailsCYAController.onPageLoad()
      }

      "must go to the next asset page if continue selected" in {
        val userAnswers = emptyAnswers.set(MoreOtherAssetsDeclarationPage, false)
        val sessionData =
          emptySessionData.set(
            SelectedAssetTypesWithStatus,
            Seq(
              SessionAssetTypeWithStatus(OtherAssetsMiniJourney.assetType, isCompleted = true),
              SessionAssetTypeWithStatus(QuotedSharesMiniJourney.assetType)
            )
          )

        val result = MoreOtherAssetsDeclarationPage.nextPageWith(NormalMode, userAnswers.success.value, sessionData.success.value)
        result mustBe QuotedSharesMiniJourney.call(NormalMode)
      }
    }

    "in CheckMode" - {
      "must go to TransferDetailsCYAController" in {
        MoreOtherAssetsDeclarationPage.nextPageWith(CheckMode, emptyAnswers, sessionDataMemberName) mustEqual
          routes.TransferDetailsCYAController.onPageLoad()
      }

      "must go to the next asset page if continue selected" in {
        val userAnswers = emptyAnswers.set(MoreOtherAssetsDeclarationPage, false)
        val sessionData =
          emptySessionData.set(
            SelectedAssetTypesWithStatus,
            Seq(
              SessionAssetTypeWithStatus(OtherAssetsMiniJourney.assetType, isCompleted = true),
              SessionAssetTypeWithStatus(QuotedSharesMiniJourney.assetType)
            )
          )

        val result = MoreOtherAssetsDeclarationPage.nextPageWith(CheckMode, userAnswers.success.value, sessionData.success.value)
        result mustBe QuotedSharesMiniJourney.call(CheckMode)
      }
    }

    "in FinalCheckMode" - {
      "must go to amend page" in {
        MoreOtherAssetsDeclarationPage.nextPage(FinalCheckMode, emptyAnswers) mustEqual
          controllers.checkYourAnswers.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go to the next asset page if continue selected" in {
        val userAnswers = emptyAnswers.set(MoreOtherAssetsDeclarationPage, false)
        val sessionData =
          emptySessionData.set(
            SelectedAssetTypesWithStatus,
            Seq(
              SessionAssetTypeWithStatus(OtherAssetsMiniJourney.assetType, isCompleted = true),
              SessionAssetTypeWithStatus(QuotedSharesMiniJourney.assetType)
            )
          )

        val result = MoreOtherAssetsDeclarationPage.nextPageWith(FinalCheckMode, userAnswers.success.value, sessionData.success.value)
        result mustBe QuotedSharesMiniJourney.call(FinalCheckMode)
      }
    }

    "in AmendCheckMode" - {
      "must go to amend page" in {
        MoreOtherAssetsDeclarationPage.nextPage(AmendCheckMode, emptyAnswers) mustEqual
          controllers.viewandamend.routes.ViewAmendSubmittedController.amend()
      }

      "must go to the next asset page if continue selected" in {
        val userAnswers = emptyAnswers.set(MoreOtherAssetsDeclarationPage, false)
        val sessionData =
          emptySessionData.set(
            SelectedAssetTypesWithStatus,
            Seq(
              SessionAssetTypeWithStatus(OtherAssetsMiniJourney.assetType, isCompleted = true),
              SessionAssetTypeWithStatus(QuotedSharesMiniJourney.assetType)
            )
          )

        val result = MoreOtherAssetsDeclarationPage.nextPageWith(AmendCheckMode, userAnswers.success.value, sessionData.success.value)
        result mustBe QuotedSharesMiniJourney.call(AmendCheckMode)
      }
    }
  }
}

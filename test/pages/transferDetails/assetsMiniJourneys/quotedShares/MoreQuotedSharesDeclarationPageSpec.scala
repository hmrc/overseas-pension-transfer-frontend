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
import controllers.transferDetails.routes
import models.assets.{QuotedSharesMiniJourney, TypeOfAsset, UnquotedSharesMiniJourney}
import models.{AmendCheckMode, CheckMode, FinalCheckMode, NormalMode, PstrNumber, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.transferDetails.TypeOfAssetPage
import queries.assets.{SelectedAssetTypesWithStatus, SessionAssetTypeWithStatus}

class MoreQuotedSharesDeclarationPageSpec extends AnyFreeSpec with Matchers with SpecBase {

  ".nextPage" - {

    val emptyAnswers = UserAnswers(userAnswersTransferNumber, PstrNumber("12345678AB"))

    "in Normal Mode" - {
      "must go to TransferDetailsCYAController" in {
        MoreQuotedSharesDeclarationPage.nextPageWith(NormalMode, emptyAnswers, sessionDataMemberName) mustEqual
          routes.TransferDetailsCYAController.onPageLoad()
      }

      "must go to the next asset page if continue selected" in {
        val userAnswers = emptyAnswers.set(MoreQuotedSharesDeclarationPage, false)
        val sessionData =
          emptySessionData.set(
            SelectedAssetTypesWithStatus,
            Seq(
              SessionAssetTypeWithStatus(QuotedSharesMiniJourney.assetType, isCompleted = true),
              SessionAssetTypeWithStatus(UnquotedSharesMiniJourney.assetType)
            )
          )

        val result = MoreQuotedSharesDeclarationPage.nextPageWith(NormalMode, userAnswers.success.value, sessionData.success.value)
        result mustBe UnquotedSharesMiniJourney.call(NormalMode)
      }
    }

    "in Check Mode" - {
      "must go to TransferDetailsCYAController" in {
        MoreQuotedSharesDeclarationPage.nextPageWith(CheckMode, emptyAnswers, sessionDataMemberName) mustEqual
          routes.TransferDetailsCYAController.onPageLoad()
      }

      "must go to the next asset page if continue selected" in {
        val userAnswers = emptyAnswers.set(MoreQuotedSharesDeclarationPage, false)
        val sessionData =
          emptySessionData.set(
            SelectedAssetTypesWithStatus,
            Seq(
              SessionAssetTypeWithStatus(QuotedSharesMiniJourney.assetType, isCompleted = true),
              SessionAssetTypeWithStatus(UnquotedSharesMiniJourney.assetType)
            )
          )

        val result = MoreQuotedSharesDeclarationPage.nextPageWith(CheckMode, userAnswers.success.value, sessionData.success.value)
        result mustBe UnquotedSharesMiniJourney.call(CheckMode)
      }
    }

    "in FinalCheckMode" - {
      "must go to amend page" in {
        MoreQuotedSharesDeclarationPage.nextPage(FinalCheckMode, emptyAnswers) mustEqual
          controllers.checkYourAnswers.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go to the next asset page if continue selected" in {
        val userAnswers = emptyAnswers.set(MoreQuotedSharesDeclarationPage, false)
        val sessionData =
          emptySessionData.set(
            SelectedAssetTypesWithStatus,
            Seq(
              SessionAssetTypeWithStatus(QuotedSharesMiniJourney.assetType, isCompleted = true),
              SessionAssetTypeWithStatus(UnquotedSharesMiniJourney.assetType)
            )
          )

        val result = MoreQuotedSharesDeclarationPage.nextPageWith(FinalCheckMode, userAnswers.success.value, sessionData.success.value)
        result mustBe UnquotedSharesMiniJourney.call(FinalCheckMode)
      }
    }

    "in AmendCheckMode" - {
      "must go to amend page" in {
        MoreQuotedSharesDeclarationPage.nextPage(AmendCheckMode, emptyAnswers) mustEqual
          controllers.viewandamend.routes.ViewAmendSubmittedController.amend()
      }

      "must go to the next asset page if continue selected" in {
        val userAnswers = emptyAnswers.set(MoreQuotedSharesDeclarationPage, false)
        val sessionData =
          emptySessionData.set(
            SelectedAssetTypesWithStatus,
            Seq(
              SessionAssetTypeWithStatus(QuotedSharesMiniJourney.assetType, isCompleted = true),
              SessionAssetTypeWithStatus(UnquotedSharesMiniJourney.assetType)
            )
          )

        val result = MoreQuotedSharesDeclarationPage.nextPageWith(AmendCheckMode, userAnswers.success.value, sessionData.success.value)
        result mustBe UnquotedSharesMiniJourney.call(AmendCheckMode)
      }
    }
  }
}

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
import models.assets.{QuotedSharesMiniJourney, UnquotedSharesMiniJourney}
import models.{AmendCheckMode, CheckMode, FinalCheckMode, NormalMode, PstrNumber, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import queries.assets.{SelectedAssetTypesWithStatus, SessionAssetTypeWithStatus}

class TypeOfAssetPageSpec extends AnyFreeSpec with SpecBase {

  ".nextPageWith" - {

    val emptyAnswers = UserAnswers(userAnswersTransferNumber, PstrNumber("12345678AB"))

    "in Normal Mode" - {

      "must go to the first incomplete asset journey when one exists" in {
        val sessionData =
          emptySessionData.set(
            SelectedAssetTypesWithStatus,
            Seq(
              SessionAssetTypeWithStatus(UnquotedSharesMiniJourney.assetType, isCompleted = false)
            )
          ).success.value

        TypeOfAssetPage.nextPageWith(NormalMode, emptyAnswers, sessionData) mustEqual
          UnquotedSharesMiniJourney.call(NormalMode)
      }

      "must go to Transfer Details CYA when all selected assets are completed or none selected" in {
        val sessionDataAllCompleted =
          emptySessionData.set(
            SelectedAssetTypesWithStatus,
            Seq(SessionAssetTypeWithStatus(UnquotedSharesMiniJourney.assetType, isCompleted = true))
          ).success.value

        TypeOfAssetPage.nextPageWith(NormalMode, emptyAnswers, sessionDataAllCompleted) mustEqual
          routes.TransferDetailsCYAController.onPageLoad()

        TypeOfAssetPage.nextPageWith(NormalMode, emptyAnswers, emptySessionData) mustEqual
          routes.TransferDetailsCYAController.onPageLoad()
      }
    }

    "in Check Mode" - {

      "must go to the first incomplete asset journey when one exists" in {
        val sessionData =
          emptySessionData.set(
            SelectedAssetTypesWithStatus,
            Seq(
              SessionAssetTypeWithStatus(UnquotedSharesMiniJourney.assetType, isCompleted = false)
            )
          ).success.value

        TypeOfAssetPage.nextPageWith(CheckMode, emptyAnswers, sessionData) mustEqual
          UnquotedSharesMiniJourney.call(CheckMode)
      }

      "must go to Transfer Details CYA when all selected assets are completed or none selected" in {
        val sessionDataAllCompleted =
          emptySessionData.set(
            SelectedAssetTypesWithStatus,
            Seq(SessionAssetTypeWithStatus(UnquotedSharesMiniJourney.assetType, isCompleted = true))
          ).success.value

        TypeOfAssetPage.nextPageWith(CheckMode, emptyAnswers, sessionDataAllCompleted) mustEqual
          routes.TransferDetailsCYAController.onPageLoad()

        TypeOfAssetPage.nextPageWith(CheckMode, emptyAnswers, emptySessionData) mustEqual
          routes.TransferDetailsCYAController.onPageLoad()
      }
    }

    "in Final Check Mode" - {

      "must go to the first incomplete asset journey when one exists" in {
        val sessionData =
          emptySessionData.set(
            SelectedAssetTypesWithStatus,
            Seq(
              SessionAssetTypeWithStatus(QuotedSharesMiniJourney.assetType, isCompleted = false)
            )
          ).success.value

        TypeOfAssetPage.nextPageWith(FinalCheckMode, emptyAnswers, sessionData) mustEqual
          QuotedSharesMiniJourney.call(FinalCheckMode)
      }

      "must go to Final Check Answers when all selected assets are completed or none selected" in {
        val sessionDataAllCompleted =
          emptySessionData.set(
            SelectedAssetTypesWithStatus,
            Seq(SessionAssetTypeWithStatus(QuotedSharesMiniJourney.assetType, isCompleted = true))
          ).success.value

        TypeOfAssetPage.nextPageWith(FinalCheckMode, emptyAnswers, sessionDataAllCompleted) mustEqual
          controllers.checkYourAnswers.routes.CheckYourAnswersController.onPageLoad()

        TypeOfAssetPage.nextPageWith(FinalCheckMode, emptyAnswers, emptySessionData) mustEqual
          controllers.checkYourAnswers.routes.CheckYourAnswersController.onPageLoad()
      }
    }

    "in Amend Check Mode" - {

      "must go to the first incomplete asset journey when one exists" in {
        val sessionData =
          emptySessionData.set(
            SelectedAssetTypesWithStatus,
            Seq(
              SessionAssetTypeWithStatus(UnquotedSharesMiniJourney.assetType, isCompleted = false)
            )
          ).success.value

        TypeOfAssetPage.nextPageWith(AmendCheckMode, emptyAnswers, sessionData) mustEqual
          UnquotedSharesMiniJourney.call(AmendCheckMode)
      }

      "must go to View & Amend CYA when all selected assets are completed or none selected" in {
        val sessionDataAllCompleted =
          emptySessionData.set(
            SelectedAssetTypesWithStatus,
            Seq(SessionAssetTypeWithStatus(UnquotedSharesMiniJourney.assetType, isCompleted = true))
          ).success.value

        TypeOfAssetPage.nextPageWith(AmendCheckMode, emptyAnswers, sessionDataAllCompleted) mustEqual
          controllers.viewandamend.routes.ViewAmendSubmittedController.amend()

        TypeOfAssetPage.nextPageWith(AmendCheckMode, emptyAnswers, emptySessionData) mustEqual
          controllers.viewandamend.routes.ViewAmendSubmittedController.amend()
      }
    }
  }
}

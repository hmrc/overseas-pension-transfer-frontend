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

package pages

import base.SpecBase
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import controllers.transferDetails.routes
import models.assets.TypeOfAsset.Cash
import models.{AmendCheckMode, CheckMode, FinalCheckMode, Mode, NormalMode, SessionData, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.transferDetails.assetsMiniJourneys.NextAssetMiniJourney
import play.api.mvc.Call
import queries.assets.{SelectedAssetTypesWithStatus, SessionAssetTypeWithStatus}

final class MiniJourneyNextPageWithSpec extends AnyFreeSpec with Matchers with SpecBase {

  private case object DummyPage extends Page with MiniJourneyNextPageWith[SessionData] with NextAssetMiniJourney {

    override protected def decideNextPage(answers: UserAnswers, ctx: SessionData, mode: Mode, modeCall: Call): Call =
      getNextAsset(ctx, mode, modeCall)

    override protected def nextPageFinalCheckMode(answers: UserAnswers): Call =
      routes.TransferDetailsCYAController.onPageLoad()

    override protected def nextPageAmendCheckMode(answers: UserAnswers): Call =
      routes.TransferDetailsCYAController.onPageLoad()
  }

  ".nextPageWith" - {

    "in NormalMode" - {

      "must fall back to the provided modeCall when there is no next asset" in {
        val sdNoSelection = emptySessionData
        val result        = DummyPage.nextPageWith(NormalMode, emptyUserAnswers, sdNoSelection).url
        result mustBe routes.TransferDetailsCYAController.onPageLoad().url
      }

      "must go to next asset when provided" in {
        val assets      = Seq(SessionAssetTypeWithStatus(Cash))
        val sdSelection = emptySessionData.set(SelectedAssetTypesWithStatus, assets).success.value
        val result      = DummyPage.nextPageWith(NormalMode, emptyUserAnswers, sdSelection).url
        result mustBe AssetsMiniJourneysRoutes.CashAmountInTransferController.onPageLoad(NormalMode).url
      }
    }

    "in CheckMode" - {

      "must fall back to the provided modeCall when there is no next asset" in {
        val sdNoSelection = emptySessionData
        val result        = DummyPage.nextPageWith(CheckMode, emptyUserAnswers, sdNoSelection).url
        result mustBe routes.TransferDetailsCYAController.onPageLoad().url
      }

      "must go to next asset when provided" in {
        val assets      = Seq(SessionAssetTypeWithStatus(Cash))
        val sdSelection = emptySessionData.set(SelectedAssetTypesWithStatus, assets).success.value
        val result      = DummyPage.nextPageWith(CheckMode, emptyUserAnswers, sdSelection).url
        result mustBe AssetsMiniJourneysRoutes.CashAmountInTransferController.onPageLoad(CheckMode).url
      }
    }

    "in FinalCheckMode" - {

      "must fall back to self.nextPageFinalCheckMode when there is no next asset" in {
        val sdNoSelection = emptySessionData
        val result        = DummyPage.nextPageWith(FinalCheckMode, emptyUserAnswers, sdNoSelection).url
        result mustBe routes.TransferDetailsCYAController.onPageLoad().url
      }

      "must go to next asset when provided" in {
        val assets      = Seq(SessionAssetTypeWithStatus(Cash))
        val sdSelection = emptySessionData.set(SelectedAssetTypesWithStatus, assets).success.value
        val result      = DummyPage.nextPageWith(FinalCheckMode, emptyUserAnswers, sdSelection).url
        result mustBe AssetsMiniJourneysRoutes.CashAmountInTransferController.onPageLoad(FinalCheckMode).url
      }
    }

    "in AmendCheckMode" - {

      "must fall back to self.nextPageAmendCheckMode when there is no next asset" in {
        val sdNoSelection = emptySessionData
        val result        = DummyPage.nextPageWith(AmendCheckMode, emptyUserAnswers, sdNoSelection).url
        result mustBe routes.TransferDetailsCYAController.onPageLoad().url
      }

      "must go to next asset when provided" in {
        val assets      = Seq(SessionAssetTypeWithStatus(Cash))
        val sdSelection = emptySessionData.set(SelectedAssetTypesWithStatus, assets).success.value
        val result      = DummyPage.nextPageWith(AmendCheckMode, emptyUserAnswers, sdSelection).url
        result mustBe AssetsMiniJourneysRoutes.CashAmountInTransferController.onPageLoad(AmendCheckMode).url
      }
    }
  }
}

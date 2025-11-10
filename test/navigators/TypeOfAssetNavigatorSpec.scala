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

package navigators

import base.SpecBase
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import models.{NormalMode, UserAnswers}
import models.assets.TypeOfAsset._
import models.assets.{QuotedSharesMiniJourney, TypeOfAsset, UnquotedSharesMiniJourney}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.transferDetails.TypeOfAssetPage
import play.api.mvc.Call
import queries.assets.AssetCompletionFlag

class TypeOfAssetNavigatorSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  "getNextAssetRoute" - {
    "must return the first uncompleted journey in order when all assets selected" in {
      val selectedTypes: Seq[TypeOfAsset] = Seq(Cash, UnquotedShares, QuotedShares, Property, TypeOfAsset.Other)
      val sessionData                     = emptySessionData.set(TypeOfAssetPage, selectedTypes).success.value

      val result = TypeOfAssetNavigator.getNextAssetRoute(sessionData, NormalMode).map(_.toString)
      result mustBe Some(AssetsMiniJourneysRoutes.CashAmountInTransferController.onPageLoad(NormalMode).url)
    }

    "must return the first uncompleted journey in order" in {
      val selectedTypes: Seq[TypeOfAsset] = Seq(UnquotedSharesMiniJourney.assetType, QuotedSharesMiniJourney.assetType)
      val sessionData                     = emptySessionData.set(TypeOfAssetPage, selectedTypes).success.value

      val result = TypeOfAssetNavigator.getNextAssetRoute(sessionData, NormalMode).map(_.toString)
      result mustBe Some(UnquotedSharesMiniJourney.call(NormalMode).url)
    }

    "must skip journeys not in the selected assets" in {
      val selectedTypes: Seq[TypeOfAsset] = Seq(QuotedSharesMiniJourney.assetType)
      val sessionData                     = emptySessionData.set(TypeOfAssetPage, selectedTypes).success.value

      val result = TypeOfAssetNavigator.getNextAssetRoute(sessionData, NormalMode).map(_.toString)
      result mustBe Some(QuotedSharesMiniJourney.call(NormalMode).url)
    }

    "must return None if all selected journeys are completed" in {
      val sessionData = emptySessionData
        .set[Seq[TypeOfAsset]](TypeOfAssetPage, Seq(UnquotedSharesMiniJourney.assetType)).success.value
        .set(AssetCompletionFlag(UnquotedSharesMiniJourney.assetType), true).success.value

      val result = TypeOfAssetNavigator.getNextAssetRoute(sessionData, NormalMode)
      result mustBe None
    }

    "must return None if no asset types have been selected" in {
      val result = TypeOfAssetNavigator.getNextAssetRoute(emptySessionData, NormalMode)
      result mustBe None
    }
  }
}

object FakePage extends pages.Page {
  override def toString: String = "fakePage"

  override protected def nextPageNormalMode(answers: UserAnswers): Call = controllers.routes.DashboardController.onPageLoad()

  override protected def nextPageCheckMode(answers: UserAnswers): Call = controllers.routes.JourneyRecoveryController.onPageLoad()
}

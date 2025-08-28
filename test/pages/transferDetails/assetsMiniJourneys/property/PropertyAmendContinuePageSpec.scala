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
import controllers.transferDetails.routes
import models.assets.TypeOfAsset.Property
import models.assets.{PropertyMiniJourney, QuotedSharesMiniJourney, TypeOfAsset}
import models.{CheckMode, NormalMode, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import pages.transferDetails.TypeOfAssetPage
import queries.assets.AssetCompletionFlag

class PropertyAmendContinuePageSpec extends AnyFreeSpec with SpecBase {

  ".nextPage" - {

    val emptyAnswers = UserAnswers("id")

    "in Normal Mode" - {
      "must go to the cya page if no more assets" in {
        PropertyAmendContinuePage.nextPage(NormalMode, emptyAnswers) mustEqual routes.TransferDetailsCYAController.onPageLoad()
      }

      "must go to the next asset page if more assets" in {
        val selectedTypes: Set[TypeOfAsset] = Set(PropertyMiniJourney.assetType, QuotedSharesMiniJourney.assetType)
        val userAnswers                     = for {
          ua1 <- emptyUserAnswers.set(TypeOfAssetPage, selectedTypes)
          ua2 <- ua1.set(AssetCompletionFlag(Property), true)
        } yield ua2

        val result = PropertyAmendContinuePage.nextPage(NormalMode, userAnswers.success.value)
        result mustBe QuotedSharesMiniJourney.call
      }
    }

    "in Check Mode" - {

      "must go to Check Answers" in {

        PropertyAmendContinuePage.nextPage(
          CheckMode,
          emptyAnswers
        ) mustEqual controllers.transferDetails.routes.TransferDetailsCYAController.onPageLoad()
      }
    }
  }
}

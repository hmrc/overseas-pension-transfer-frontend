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
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import models.{AmendCheckMode, CheckMode, FinalCheckMode, NormalMode, PstrNumber, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class OtherAssetsValuePageSpec extends AnyFreeSpec with Matchers with SpecBase {
  private val index = 0
  ".nextPage" - {
    val emptyAnswers = UserAnswers(userAnswersTransferNumber, PstrNumber("12345678AB"))

    "in NormalMode" - {
      "must go to the Next page" in {
        OtherAssetsValuePage(index).nextPage(NormalMode, emptyAnswers) mustEqual AssetsMiniJourneysRoutes.OtherAssetsCYAController.onPageLoad(NormalMode, index)
      }
    }

    "in CheckMode" - {
      "must go to Check Answers" in {
        OtherAssetsValuePage(index).nextPage(CheckMode, emptyAnswers) mustEqual AssetsMiniJourneysRoutes.OtherAssetsCYAController.onPageLoad(CheckMode, index)
      }
    }

    "in FinalCheckMode" - {
      "must go to Final Check Answers page" in {
        OtherAssetsValuePage(index).nextPage(FinalCheckMode, emptyAnswers) mustEqual AssetsMiniJourneysRoutes.OtherAssetsCYAController.onPageLoad(
          FinalCheckMode,
          index
        )
      }
    }

    "in AmendCheckMode" - {
      "must go to Final Check Answers page" in {
        OtherAssetsValuePage(index).nextPage(AmendCheckMode, emptyAnswers) mustEqual AssetsMiniJourneysRoutes.OtherAssetsCYAController.onPageLoad(
          AmendCheckMode,
          index
        )
      }
    }
  }
}

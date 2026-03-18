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
import models.assets.TypeOfAsset
import models.{AmendCheckMode, CheckMode, FinalCheckMode, NormalMode, PstrNumber, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class PropertyAddressPageSpec extends AnyFreeSpec with Matchers with SpecBase {
  private val index = 0

  ".nextPage" - {

    "in Normal Mode" - {

      "must go to next page" in {
        PropertyAddressPage(index).nextPage(NormalMode, emptyUserAnswers) mustEqual AssetsMiniJourneysRoutes.PropertyValueController.onPageLoad(
          NormalMode,
          index
        )
      }
    }

    "in CheckMode" - {

      "must go to next page" in {
        PropertyAddressPage(index).nextPage(CheckMode, emptyUserAnswers) mustEqual AssetsMiniJourneysRoutes.PropertyValueController.onPageLoad(CheckMode, index)
      }
    }

    "in FinalCheckMode" - {
      "must go to next page" in {
        PropertyAddressPage(index).nextPage(FinalCheckMode, emptyUserAnswers) mustEqual AssetsMiniJourneysRoutes.PropertyValueController.onPageLoad(
          FinalCheckMode,
          index
        )
      }
    }

    "in AmendCheckMode" - {
      "must go to next page" in {
        PropertyAddressPage(index).nextPage(AmendCheckMode, emptyUserAnswers) mustEqual AssetsMiniJourneysRoutes.PropertyValueController.onPageLoad(
          AmendCheckMode,
          index
        )
      }
    }

    "should go to CYA when complete" in {
      val ua = emptyUserAnswers.copy(data = completeJson(TypeOfAsset.Property))
      PropertyAddressPage(index).nextPage(NormalMode, ua) mustBe AssetsMiniJourneysRoutes.PropertyCYAController.onPageLoad(NormalMode, index)
    }

    "should go to PropertyValue when incomplete" in {
      val ua = emptyUserAnswers.copy(data = incompleteJson())
      PropertyAddressPage(index).nextPage(NormalMode, ua) mustBe AssetsMiniJourneysRoutes.PropertyValueController.onPageLoad(NormalMode, index)
    }
  }
}

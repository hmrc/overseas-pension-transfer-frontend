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
import models.{AmendCheckMode, CheckMode, FinalCheckMode, NormalMode, PstrNumber, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import uaOps.UAOps.Assets

class PropertyCYAPageSpec extends AnyFreeSpec with Matchers with SpecBase {
  private val index = 0

  ".nextPage" - {

    val emptyAnswers = UserAnswers(userAnswersTransferNumber, PstrNumber("12345678AB"))

    val moreThan5Ua = (0 to 5).foldLeft(emptyAnswers)((ua, idx) => ua.withPropertyAsset(idx))

    "in Normal Mode" - {
      "must go to AmendContinue" in {
        PropertyCYAPage(index).nextPage(NormalMode, emptyAnswers) mustEqual AssetsMiniJourneysRoutes.PropertyAmendContinueController.onPageLoad(
          NormalMode
        )
      }
      "must go to more page if more than 5 assets" in {
        PropertyCYAPage(index).nextPage(NormalMode, moreThan5Ua) mustEqual AssetsMiniJourneysRoutes.MorePropertyDeclarationController.onPageLoad(
          NormalMode
        )
      }
    }

    "in CheckMode" - {
      "must go to AmendContinue" in {
        PropertyCYAPage(index).nextPage(CheckMode, emptyAnswers) mustEqual AssetsMiniJourneysRoutes.PropertyAmendContinueController.onPageLoad(
          CheckMode
        )
      }
      "must go to more page if more than 5 assets" in {
        PropertyCYAPage(index).nextPage(CheckMode, moreThan5Ua) mustEqual AssetsMiniJourneysRoutes.MorePropertyDeclarationController.onPageLoad(
          CheckMode
        )
      }
    }

    "in FinalCheckMode" - {
      "must go to AmendContinue" in {
        PropertyCYAPage(index).nextPage(FinalCheckMode, emptyAnswers) mustEqual AssetsMiniJourneysRoutes.PropertyAmendContinueController.onPageLoad(
          FinalCheckMode
        )
      }
      "must go to more page if more than 5 assets" in {
        PropertyCYAPage(index).nextPage(FinalCheckMode, moreThan5Ua) mustEqual AssetsMiniJourneysRoutes.MorePropertyDeclarationController.onPageLoad(
          FinalCheckMode
        )
      }
    }

    "in AmendCheckMode" - {
      "must go to AmendContinue" in {
        PropertyCYAPage(index).nextPage(AmendCheckMode, emptyAnswers) mustEqual AssetsMiniJourneysRoutes.PropertyAmendContinueController.onPageLoad(
          AmendCheckMode
        )
      }
      "must go to more page if more than 5 assets" in {
        PropertyCYAPage(index).nextPage(AmendCheckMode, moreThan5Ua) mustEqual AssetsMiniJourneysRoutes.MorePropertyDeclarationController.onPageLoad(
          AmendCheckMode
        )
      }
    }
  }
}

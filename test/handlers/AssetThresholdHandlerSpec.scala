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

package handlers

import base.SpecBase
import models.assets.TypeOfAsset
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class AssetThresholdHandlerSpec extends AnyFreeSpec with Matchers with SpecBase {

  private val handler = new AssetThresholdHandler()

  "AssetThresholdHandler" - {

    "getAssetCount" - {

      "must return 0 if no assets present" in {
        val userAnswers = emptyUserAnswers
        handler.getAssetCount(userAnswers, TypeOfAsset.Property) mustEqual 0
      }

      "must return correct count for given asset type" in {
        val userAnswers = userAnswersWithProperty(2)
        handler.getAssetCount(userAnswers, TypeOfAsset.Property) mustEqual 2
      }

      "must return 0 for unknown asset type (no data)" in {
        val userAnswers = emptyUserAnswers
        handler.getAssetCount(userAnswers, TypeOfAsset.QuotedShares) mustEqual 0
      }
    }

    "handle" - {

      "must set flag false if asset count < threshold" in {
        val userAnswers = userAnswersWithProperty(3)
        val updated     = handler.handle(userAnswers, TypeOfAsset.Property)
        (updated.data \ "transferDetails" \ "moreProp").as[Boolean] mustBe false
      }

      "must set flag to userSelection if asset count == threshold" in {
        val userAnswers = userAnswersWithProperty(5)
        val updatedTrue = handler.handle(userAnswers, TypeOfAsset.Property, Some(true))
        (updatedTrue.data \ "transferDetails" \ "moreProp").as[Boolean] mustBe true

        val updatedFalse = handler.handle(userAnswers, TypeOfAsset.Property, Some(false))
        (updatedFalse.data \ "transferDetails" \ "moreProp").as[Boolean] mustBe false
      }

      "must set flag false if asset count > threshold" in {
        val userAnswers = userAnswersWithProperty(6)
        val updated     = handler.handle(userAnswers, TypeOfAsset.Property)
        (updated.data \ "transferDetails" \ "moreProp").as[Boolean] mustBe false
      }

      "must set flag false even if no assets for known asset type" in {
        val userAnswers = emptyUserAnswers
        val updated     = handler.handle(userAnswers, TypeOfAsset.QuotedShares)
        (updated.data \ "transferDetails" \ "moreQuoted").as[Boolean] mustBe false
      }

      "must return unchanged UserAnswers for unknown asset type" in {
        val userAnswers = emptyUserAnswers
        val updated     = handler.handle(userAnswers, TypeOfAsset.Other)
        (updated.data \ "transferDetails" \ "moreAsset").asOpt[Boolean] mustBe Some(false)
      }
    }
  }
}

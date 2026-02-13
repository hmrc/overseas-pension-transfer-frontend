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

package validators.assetsValidators

import base.SpecBase
import models.UserAnswers
import models.assets.TypeOfAsset
import org.scalatest.freespec.AnyFreeSpec
import play.api.libs.json._

class AssetCompletionValidatorSpec extends AnyFreeSpec with SpecBase {

  private def ua(json: JsObject): UserAnswers = emptyUserAnswers.copy(data = json)

  "AssetCompletionValidator.hasMandatoryFields" - {

    "Cash asset" - {
      "return true when cashValue is present" in {
        val json = Json.obj(
          "transferDetails" -> Json.obj(
            "cashValue" -> 10000
          )
        )

        AssetCompletionValidator.hasMandatoryFields(TypeOfAsset.Cash, ua(json)) mustBe true
      }

      "return false when cashValue is missing" in {
        val json = Json.obj("transferDetails" -> Json.obj())

        AssetCompletionValidator.hasMandatoryFields(TypeOfAsset.Cash, ua(json)) mustBe false
      }
    }

    "UnquotedShares asset" - {
      "return true when all mandatory fields exist" in {
        val json = Json.obj(
          "transferDetails" -> Json.obj(
            "unquotedShares" -> Json.arr(
              Json.obj(
                "unquotedValue"      -> 1000,
                "unquotedShareTotal" -> 10,
                "unquotedCompany"    -> "ABC Ltd",
                "unquotedClass"      -> "A"
              )
            )
          )
        )

        AssetCompletionValidator.hasMandatoryFields(TypeOfAsset.UnquotedShares, ua(json)) mustBe true
      }

      "return false when one mandatory field is missing" in {
        val json = Json.obj(
          "transferDetails" -> Json.obj(
            "unquotedShares" -> Json.arr(
              Json.obj(
                "unquotedValue"      -> 1000,
                "unquotedShareTotal" -> 10,
                "unquotedCompany"    -> "ABC Ltd"
              )
            )
          )
        )

        AssetCompletionValidator.hasMandatoryFields(TypeOfAsset.UnquotedShares, ua(json)) mustBe false
      }
    }

    "QuotedShares asset" - {
      "return true when all mandatory fields exist" in {
        val json = Json.obj(
          "transferDetails" -> Json.obj(
            "quotedShares" -> Json.arr(
              Json.obj(
                "quotedValue"      -> 2000,
                "quotedShareTotal" -> 20,
                "quotedCompany"    -> "XYZ PLC",
                "quotedClass"      -> "B"
              )
            )
          )
        )

        AssetCompletionValidator.hasMandatoryFields(TypeOfAsset.QuotedShares, ua(json)) mustBe true
      }

      "return false when quotedShares array is empty" in {
        val json = Json.obj(
          "transferDetails" -> Json.obj(
            "quotedShares" -> Json.arr()
          )
        )

        AssetCompletionValidator.hasMandatoryFields(TypeOfAsset.QuotedShares, ua(json)) mustBe false
      }
    }

    "Property asset" - {
      "return true when all mandatory property fields exist" in {
        val json = Json.obj(
          "transferDetails" -> Json.obj(
            "propertyAssets" -> Json.arr(
              Json.obj(
                "propertyAddress" -> "Line 1",
                "propValue"       -> 250000,
                "propDescription" -> "Residential house"
              )
            )
          )
        )

        AssetCompletionValidator.hasMandatoryFields(TypeOfAsset.Property, ua(json)) mustBe true
      }

      "return false when propDescription is missing" in {
        val json = Json.obj(
          "transferDetails" -> Json.obj(
            "propertyAssets" -> Json.arr(
              Json.obj(
                "propertyAddress" -> "Line 1",
                "propValue"       -> 250000
              )
            )
          )
        )

        AssetCompletionValidator.hasMandatoryFields(TypeOfAsset.Property, ua(json)) mustBe false
      }
    }

    "Other asset" - {
      "return true when all mandatory fields exist" in {
        val json = Json.obj(
          "transferDetails" -> Json.obj(
            "otherAssets" -> Json.arr(
              Json.obj(
                "assetValue"       -> 500,
                "assetDescription" -> "Gold coins"
              )
            )
          )
        )

        AssetCompletionValidator.hasMandatoryFields(TypeOfAsset.Other, ua(json)) mustBe true
      }

      "return false when assetDescription is empty" in {
        val json = Json.obj(
          "transferDetails" -> Json.obj(
            "otherAssets" -> Json.arr(
              Json.obj(
                "assetValue"       -> 500,
                "assetDescription" -> ""
              )
            )
          )
        )

        AssetCompletionValidator.hasMandatoryFields(TypeOfAsset.Other, ua(json)) mustBe false
      }
    }

    "return false when transferDetails is missing" in {
      val json = Json.obj()

      AssetCompletionValidator.hasMandatoryFields(TypeOfAsset.Property, ua(json)) mustBe false
    }

    "return false for unsupported asset type" in {
      val json = Json.obj("transferDetails" -> Json.obj("cashValue" -> 100))

      AssetCompletionValidator.hasMandatoryFields(null, ua(json)) mustBe false
    }
  }
}

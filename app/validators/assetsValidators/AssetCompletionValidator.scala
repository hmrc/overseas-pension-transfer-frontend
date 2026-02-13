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

import models.UserAnswers
import models.assets.TypeOfAsset
import play.api.libs.json._

object AssetCompletionValidator {

  private def isNonEmpty(js: JsValue): Boolean = js match {
    case JsString(s)      => s.trim.nonEmpty
    case JsNumber(_)      => true
    case JsArray(arr)     => arr.nonEmpty
    case JsObject(fields) => fields.nonEmpty
    case JsBoolean(_)     => true
    case _                => false
  }

  private def hasMandatoryKeys(root: JsValue, keys: Seq[String]): Boolean =
    keys.forall { key =>
      (root \ key).toOption.exists(isNonEmpty)
    }

  val mandatoryKeys: Map[TypeOfAsset, Seq[String]] = Map(
    TypeOfAsset.Cash           -> Seq("cashValue"),
    TypeOfAsset.UnquotedShares -> Seq(
      "unquotedValue",
      "unquotedShareTotal",
      "unquotedCompany",
      "unquotedClass"
    ),
    TypeOfAsset.QuotedShares   -> Seq(
      "quotedValue",
      "quotedShareTotal",
      "quotedCompany",
      "quotedClass"
    ),
    TypeOfAsset.Property       -> Seq(
      "propertyAddress",
      "propValue",
      "propDescription"
    ),
    TypeOfAsset.Other          -> Seq(
      "assetValue",
      "assetDescription"
    )
  )

  def hasMandatoryFields(assetType: TypeOfAsset, ua: UserAnswers): Boolean = {
    val td: JsValue = (ua.data \ "transferDetails").getOrElse(Json.obj())

    assetType match {

      case TypeOfAsset.Cash =>
        hasMandatoryKeys(td, mandatoryKeys(TypeOfAsset.Cash))

      case TypeOfAsset.UnquotedShares =>
        (td \ "unquotedShares").asOpt[JsArray]
          .exists(arr => arr.value.nonEmpty && arr.value.forall(js => hasMandatoryKeys(js, mandatoryKeys(TypeOfAsset.UnquotedShares))))

      case TypeOfAsset.QuotedShares =>
        (td \ "quotedShares").asOpt[JsArray]
          .exists(arr => arr.value.nonEmpty && arr.value.forall(js => hasMandatoryKeys(js, mandatoryKeys(TypeOfAsset.QuotedShares))))

      case TypeOfAsset.Property =>
        (td \ "propertyAssets").asOpt[JsArray]
          .exists(arr => arr.value.nonEmpty && arr.value.forall(js => hasMandatoryKeys(js, mandatoryKeys(TypeOfAsset.Property))))

      case TypeOfAsset.Other =>
        (td \ "otherAssets").asOpt[JsArray]
          .exists(arr => arr.value.nonEmpty && arr.value.forall(js => hasMandatoryKeys(js, mandatoryKeys(TypeOfAsset.Other))))

      case _ => false
    }
  }
}

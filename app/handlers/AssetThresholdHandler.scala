/*
 * Copyright 2024 HM Revenue & Customs
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

import models.{RichJsObject, SessionData, UserAnswers}
import models.assets.TypeOfAsset
import play.api.libs.json._

class AssetThresholdHandler {

  private val AssetThresholdLimit: Int = 5

  private val assetChecks: Map[TypeOfAsset, (String, String)] = Map(
    TypeOfAsset.Property       -> ("propertyAssets", "moreProp"),
    TypeOfAsset.Other          -> ("otherAssets", "moreAsset"),
    TypeOfAsset.UnquotedShares -> ("unquotedShares", "moreUnquoted"),
    TypeOfAsset.QuotedShares   -> ("quotedShares", "moreQuoted")
  )

  /** Get count of assets of given type */
  def getAssetCount(sessionData: SessionData, assetType: TypeOfAsset): Int =
    assetChecks.get(assetType).map { case (assetKey, _) =>
      (sessionData.data \ "transferDetails" \ assetKey)
        .asOpt[Seq[JsValue]]
        .map(_.size)
        .getOrElse(0)
    }.getOrElse(0)

  /** Update threshold flag. If userSelection is Some(true/false), it overrides the flag when count == threshold. Otherwise:
    *   - count < threshold → false
    *   - count == threshold → userSelection (or false if not provided)
    */
  def handle(
      sessionData: SessionData,
      assetType: TypeOfAsset,
      userSelection: Option[Boolean] = None
    ): SessionData = {

    assetChecks.get(assetType) match {
      case Some((_, flagKey)) =>
        val assetCount = getAssetCount(sessionData, assetType)

        val flag = assetCount match {
          case count if count < AssetThresholdLimit  => false
          case count if count == AssetThresholdLimit => userSelection.getOrElse(false)
          case _                                     => false
        }

        val updatedData = sessionData.data
          .setObject(JsPath \ "transferDetails" \ flagKey, Json.toJson(flag))
          .getOrElse(sessionData.data)

        sessionData.copy(data = updatedData)

      case None => sessionData
    }
  }
}

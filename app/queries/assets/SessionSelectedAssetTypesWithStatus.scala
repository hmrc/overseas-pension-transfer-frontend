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

package queries.assets

import models.{SessionData, TaskCategory}
import models.assets.TypeOfAsset
import play.api.libs.json._
import play.api.libs.functional.syntax._
import queries.{Gettable, Settable}

case class SessionAssetTypeWithStatus(
    assetType: TypeOfAsset,
    isCompleted: Boolean = false
  )

object SessionAssetTypeWithStatus {
  implicit val format: Format[SessionAssetTypeWithStatus] = (
    (__ \ "type").format[TypeOfAsset] and
      (__ \ "isCompleted").format[Boolean]
  )(SessionAssetTypeWithStatus.apply, unlift(SessionAssetTypeWithStatus.unapply))
}

case object SelectedAssetTypesWithStatus extends Gettable[Seq[SessionAssetTypeWithStatus]] with Settable[Seq[SessionAssetTypeWithStatus]] {
  override def path: JsPath = JsPath \ TaskCategory.TransferDetails.toString \ "typeOfAsset"

  def fromTypes(types: Seq[TypeOfAsset]): Seq[SessionAssetTypeWithStatus] =
    types.map(SessionAssetTypeWithStatus(_, isCompleted = false))

  def toTypes(typesWithStatus: Seq[SessionAssetTypeWithStatus]): Seq[TypeOfAsset] =
    typesWithStatus.map(_.assetType)

  def getCompletedAssets(assets: Seq[SessionAssetTypeWithStatus]): Seq[TypeOfAsset] =
    assets.filter(_.isCompleted).map(_.assetType)

  def getIncompleteAssets(assets: Seq[SessionAssetTypeWithStatus]): Seq[TypeOfAsset] =
    assets.filterNot(_.isCompleted).map(_.assetType)

  def markAsCompleted(assets: Seq[SessionAssetTypeWithStatus], assetType: TypeOfAsset): Seq[SessionAssetTypeWithStatus] =
    assets.map(a => if (a.assetType == assetType) a.copy(isCompleted = true) else a)

  def markAsIncomplete(assets: Seq[SessionAssetTypeWithStatus], assetType: TypeOfAsset): Seq[SessionAssetTypeWithStatus] =
    assets.map(a => if (a.assetType == assetType) a.copy(isCompleted = false) else a)

  def markAllIncomplete(assets: Seq[SessionAssetTypeWithStatus]): Seq[SessionAssetTypeWithStatus] =
    assets.map(_.copy(isCompleted = false))
}

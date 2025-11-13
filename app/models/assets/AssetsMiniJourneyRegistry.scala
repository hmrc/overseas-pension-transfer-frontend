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

package models.assets

import models.{Mode, SessionData}
import play.api.mvc.Call
import queries.assets.SelectedAssetTypesWithStatus

object AssetsMiniJourneyRegistry {

  val all: Seq[AssetsMiniJourneyBase] = Seq(
    CashMiniJourney,
    UnquotedSharesMiniJourney,
    QuotedSharesMiniJourney,
    PropertyMiniJourney,
    OtherAssetsMiniJourney
  )

  def repeating: Seq[RepeatingAssetsMiniJourney[_]] =
    all.collect { case r: RepeatingAssetsMiniJourney[_] => r }

  def forType(assetType: TypeOfAsset): Option[AssetsMiniJourneyBase] =
    all.find(_.assetType == assetType)

  def firstIncompleteJourney(sessionData: SessionData): Option[AssetsMiniJourneyBase] = {
    val sd = sessionData.get(SelectedAssetTypesWithStatus).getOrElse(Seq.empty)
    SelectedAssetTypesWithStatus.getIncompleteAssets(sd).flatMap(forType).headOption
  }

  def startOf(assetType: TypeOfAsset, mode: Mode, index: Int): Call =
    forType(assetType).map(_.call(mode, index))
      .getOrElse(controllers.routes.JourneyRecoveryController.onPageLoad())
}

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

import models.{TypeOfAsset, UserAnswers}
import queries.assets.SelectedAssetTypes

object AssetsMiniJourneyRegistry {

  val all: Seq[AssetsMiniJourney[_]] = Seq(
    UnquotedSharesMiniJourney,
    QuotedSharesMiniJourney
  )

  def forType(assetType: TypeOfAsset): Option[AssetsMiniJourney[_]] =
    all.find(_.assetType == assetType)

  def selectedJourneys(userAnswers: UserAnswers): Seq[AssetsMiniJourney[_]] =
    userAnswers.get(SelectedAssetTypes).toSeq.flatten.flatMap(forType)

  def firstIncompleteJourney(userAnswers: UserAnswers): Option[AssetsMiniJourney[_]] =
    selectedJourneys(userAnswers).find(!_.isCompleted(userAnswers))
}

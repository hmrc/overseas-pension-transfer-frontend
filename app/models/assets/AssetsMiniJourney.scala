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

import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes._
import models.{NormalMode, UserAnswers}
import play.api.libs.json.OFormat
import play.api.mvc.Call
import queries.assets._

sealed trait AssetsMiniJourneyBase {
  def assetType: TypeOfAsset
  def startPage: () => Call
  def isCompleted(answers: UserAnswers): Boolean

  final def call: Call = startPage()
}

trait RepeatingAssetsMiniJourney[A <: AssetEntry] extends AssetsMiniJourneyBase {
  def query: AssetsQuery[List[A]]
  def format: OFormat[A]
}

trait SingleAssetsMiniJourney[A <: AssetEntry] extends AssetsMiniJourneyBase {
  def query: AssetsQuery[A]
  def format: OFormat[A]
}

object CashMiniJourney extends SingleAssetsMiniJourney[CashEntry] {
  val assetType = TypeOfAsset.Cash
  val query     = CashQuery
  val format    = CashEntry.format
  val startPage = () => CashAmountInTransferController.onPageLoad(NormalMode)

  def isCompleted(ua: UserAnswers): Boolean =
    ua.get(AssetCompletionFlag(assetType)).contains(true)
}

object QuotedSharesMiniJourney extends RepeatingAssetsMiniJourney[QuotedSharesEntry] {
  val assetType = TypeOfAsset.QuotedShares
  val query     = QuotedSharesQuery
  val format    = QuotedSharesEntry.format
  val startPage = () => QuotedSharesStartController.onPageLoad()

  def isCompleted(ua: UserAnswers): Boolean =
    ua.get(AssetCompletionFlag(assetType)).contains(true)
}

object UnquotedSharesMiniJourney extends RepeatingAssetsMiniJourney[UnquotedSharesEntry] {
  val assetType = TypeOfAsset.UnquotedShares
  val query     = UnquotedSharesQuery
  val format    = UnquotedSharesEntry.format
  val startPage = () => UnquotedSharesStartController.onPageLoad()

  def isCompleted(ua: UserAnswers): Boolean =
    ua.get(AssetCompletionFlag(assetType)).contains(true)
}

object PropertyMiniJourney extends RepeatingAssetsMiniJourney[PropertyEntry] {
  val assetType = TypeOfAsset.Property
  val query     = PropertyQuery
  val format    = PropertyEntry.format
  val startPage = () => PropertyStartController.onPageLoad()

  def isCompleted(ua: UserAnswers): Boolean =
    ua.get(AssetCompletionFlag(assetType)).contains(true)
}

object OtherAssetsMiniJourney extends RepeatingAssetsMiniJourney[OtherAssetsEntry] {
  val assetType = TypeOfAsset.Other
  val query     = OtherAssetsQuery
  val format    = OtherAssetsEntry.format
  val startPage = () => OtherAssetsStartController.onPageLoad()

  def isCompleted(ua: UserAnswers): Boolean =
    ua.get(AssetCompletionFlag(assetType)).contains(true)
}

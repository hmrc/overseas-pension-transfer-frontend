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
import models.{Mode, NormalMode, SessionData, UserAnswers}
import play.api.libs.json.OFormat
import play.api.mvc.Call
import queries.assets._

sealed trait AssetsMiniJourneyBase {
  def assetType: TypeOfAsset
  def isCompleted(session: SessionData): Boolean
  def startPage: Mode => Call
  final def call(mode: Mode): Call = startPage(mode)

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
  val assetType                        = TypeOfAsset.Cash
  val query                            = CashQuery
  val format                           = CashEntry.format
  override def startPage: Mode => Call = (mode: Mode) => CashAmountInTransferController.onPageLoad(mode)

  def isCompleted(sd: SessionData): Boolean =
    sd.get(AssetCompletionFlag(assetType)).contains(true)
}

object QuotedSharesMiniJourney extends RepeatingAssetsMiniJourney[QuotedSharesEntry] {
  val assetType = TypeOfAsset.QuotedShares
  val query     = QuotedSharesQuery
  val format    = QuotedSharesEntry.format

  def isCompleted(sd: SessionData): Boolean =
    sd.get(AssetCompletionFlag(assetType)).contains(true)

  override def startPage: Mode => Call = {
    case NormalMode => QuotedSharesStartController.onPageLoad()
    case mode       => QuotedSharesCompanyNameController.onPageLoad(mode, 0)
    case _          => controllers.routes.JourneyRecoveryController.onPageLoad()
  }
}

object UnquotedSharesMiniJourney extends RepeatingAssetsMiniJourney[UnquotedSharesEntry] {
  val assetType = TypeOfAsset.UnquotedShares
  val query     = UnquotedSharesQuery
  val format    = UnquotedSharesEntry.format

  override def startPage: Mode => Call = {
    case NormalMode => UnquotedSharesStartController.onPageLoad()
    case mode       => UnquotedSharesCompanyNameController.onPageLoad(mode, 0)
    case _          => controllers.routes.JourneyRecoveryController.onPageLoad()
  }

  def isCompleted(sd: SessionData): Boolean =
    sd.get(AssetCompletionFlag(assetType)).contains(true)
}

object PropertyMiniJourney extends RepeatingAssetsMiniJourney[PropertyEntry] {
  val assetType = TypeOfAsset.Property
  val query     = PropertyQuery
  val format    = PropertyEntry.format

  override def startPage: Mode => Call = {
    case NormalMode => PropertyStartController.onPageLoad()
    case mode       => PropertyAddressController.onPageLoad(mode, 0)
    case _          => controllers.routes.JourneyRecoveryController.onPageLoad()
  }

  def isCompleted(sd: SessionData): Boolean =
    sd.get(AssetCompletionFlag(assetType)).contains(true)
}

object OtherAssetsMiniJourney extends RepeatingAssetsMiniJourney[OtherAssetsEntry] {
  val assetType = TypeOfAsset.Other
  val query     = OtherAssetsQuery
  val format    = OtherAssetsEntry.format

  override def startPage: Mode => Call = {
    case NormalMode => OtherAssetsStartController.onPageLoad()
    case mode       => OtherAssetsDescriptionController.onPageLoad(mode, 0)
    case _          => controllers.routes.JourneyRecoveryController.onPageLoad()
  }

  def isCompleted(sd: SessionData): Boolean =
    sd.get(AssetCompletionFlag(assetType)).contains(true)
}

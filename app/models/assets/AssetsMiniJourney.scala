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
import models.{AssetEntry, PropertyEntry, QuotedSharesEntry, TypeOfAsset, UnquotedSharesEntry, UserAnswers}
import play.api.libs.json.OFormat
import play.api.mvc.Call
import queries.assets._

sealed trait AssetsMiniJourney[A <: AssetEntry] {
  def assetType: TypeOfAsset
  def query: AssetsQuery[List[A]]
  def format: OFormat[A]
  def startPage: () => Call
  def isCompleted(answers: UserAnswers): Boolean

  def call: Call = startPage()
}

object QuotedSharesMiniJourney extends AssetsMiniJourney[QuotedSharesEntry] {
  val assetType = TypeOfAsset.QuotedShares
  val query     = QuotedSharesQuery
  val format    = QuotedSharesEntry.format
  val startPage = () => QuotedSharesStartController.onPageLoad()

  def isCompleted(ua: UserAnswers): Boolean =
    ua.get(AssetCompletionFlag(assetType)).contains(true)
}

object UnquotedSharesMiniJourney extends AssetsMiniJourney[UnquotedSharesEntry] {
  val assetType = TypeOfAsset.UnquotedShares
  val query     = UnquotedSharesQuery
  val format    = UnquotedSharesEntry.format
  val startPage = () => UnquotedSharesStartController.onPageLoad()

  def isCompleted(ua: UserAnswers): Boolean =
    ua.get(AssetCompletionFlag(assetType)).contains(true)
}

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

package pages.transferDetails.assetsMiniJourneys.quotedShares

import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import models.assets.{QuotedSharesEntry, TypeOfAsset}
import models.{AmendCheckMode, CheckMode, FinalCheckMode, Mode, NormalMode, TaskCategory, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class QuotedSharesValuePage(index: Int) extends QuestionPage[BigDecimal] {

  override def path: JsPath = JsPath \ TaskCategory.TransferDetails.toString \ TypeOfAsset.QuotedShares.entryName \ index \ toString

  override def toString: String = QuotedSharesEntry.ValueOfShares

  override protected def nextPageNormalMode(answers: UserAnswers): Call =
    AssetsMiniJourneysRoutes.QuotedSharesNumberController.onPageLoad(NormalMode, index)

  override protected def nextPageCheckMode(answers: UserAnswers): Call =
    AssetsMiniJourneysRoutes.QuotedSharesNumberController.onPageLoad(CheckMode, index)

  override protected def nextPageFinalCheckMode(answers: UserAnswers): Call =
    AssetsMiniJourneysRoutes.QuotedSharesNumberController.onPageLoad(FinalCheckMode, index)

  override protected def nextPageAmendCheckMode(answers: UserAnswers): Call =
    AssetsMiniJourneysRoutes.QuotedSharesNumberController.onPageLoad(AmendCheckMode, index)

  final def changeLink(mode: Mode): Call =
    AssetsMiniJourneysRoutes.QuotedSharesValueController.onPageLoad(mode, index)
}

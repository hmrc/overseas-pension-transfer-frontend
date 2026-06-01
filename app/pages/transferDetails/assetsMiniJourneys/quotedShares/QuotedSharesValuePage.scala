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

import play.api.mvc.Call
import pages.MiniJourneyNextPage
import pages.QuestionPage
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import models.Mode
import models.TaskCategory
import models.UserAnswers
import validators.assetsValidators.AssetCompletionValidator
import play.api.libs.json.JsPath
import models.assets.QuotedSharesEntry
import models.assets.TypeOfAsset

case class QuotedSharesValuePage(index: Int) extends QuestionPage[BigDecimal] with MiniJourneyNextPage {

  override def path: JsPath =
    JsPath \ TaskCategory.TransferDetails.toString \ TypeOfAsset.QuotedShares.entryName \ index \ toString

  override def toString: String = QuotedSharesEntry.ValueOfShares

  override def decideNextPage(answers: UserAnswers, mode: Mode): Call =
    if (AssetCompletionValidator.hasMandatoryFields(TypeOfAsset.QuotedShares, answers)) {
      AssetsMiniJourneysRoutes.QuotedSharesCYAController.onPageLoad(mode, index)
    } else {
      AssetsMiniJourneysRoutes.QuotedSharesNumberController.onPageLoad(mode, index)
    }

  final def changeLink(mode: Mode): Call =
    AssetsMiniJourneysRoutes.QuotedSharesValueController.onPageLoad(mode, index)
}

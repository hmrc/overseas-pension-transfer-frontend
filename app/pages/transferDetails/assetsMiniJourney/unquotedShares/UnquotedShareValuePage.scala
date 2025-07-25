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

package pages.transferDetails.assetsMiniJourney.unquotedShares

import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import models.{CheckMode, NormalMode, ShareEntry, TaskCategory, TypeOfAsset, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class UnquotedShareValuePage(index: Int) extends QuestionPage[BigDecimal] {

  override def path: JsPath = JsPath \ TaskCategory.TransferDetails.toString \ TypeOfAsset.UnquotedShares.toString \ index \ toString

  override def toString: String = ShareEntry.ValueOfShares

  override protected def nextPageNormalMode(answers: UserAnswers): Call =
    AssetsMiniJourneysRoutes.NumberOfUnquotedSharesController.onPageLoad(NormalMode, index)

  override protected def nextPageCheckMode(answers: UserAnswers): Call =
    AssetsMiniJourneysRoutes.UnquotedShareCYAController.onPageLoad(index)

  final def changeLink(answers: UserAnswers): Call =
    AssetsMiniJourneysRoutes.UnquotedShareValueController.onPageLoad(CheckMode, index)
}

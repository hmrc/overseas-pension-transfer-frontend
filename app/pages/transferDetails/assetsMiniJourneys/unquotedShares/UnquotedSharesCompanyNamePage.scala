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

package pages.transferDetails.assetsMiniJourneys.unquotedShares

import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import models.assets.{TypeOfAsset, UnquotedSharesEntry}
import models.{CheckMode, NormalMode, TaskCategory, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class UnquotedSharesCompanyNamePage(index: Int) extends QuestionPage[String] {

  override def path: JsPath = JsPath \ TaskCategory.TransferDetails.toString \ TypeOfAsset.UnquotedShares.toString \ index \ toString

  override def toString: String = UnquotedSharesEntry.CompanyName

  override protected def nextPageNormalMode(answers: UserAnswers): Call =
    AssetsMiniJourneysRoutes.UnquotedSharesValueController.onPageLoad(NormalMode, index)

  override protected def nextPageCheckMode(answers: UserAnswers): Call =
    AssetsMiniJourneysRoutes.UnquotedSharesCYAController.onPageLoad(index)

  final def changeLink(answers: UserAnswers): Call =
    AssetsMiniJourneysRoutes.UnquotedSharesCompanyNameController.onPageLoad(CheckMode, index)
}

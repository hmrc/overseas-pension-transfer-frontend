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

package pages.transferDetails.assetsMiniJourneys.property

import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import models.assets.{PropertyEntry, TypeOfAsset}
import models.{AmendCheckMode, CheckMode, FinalCheckMode, Mode, NormalMode, TaskCategory, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class PropertyDescriptionPage(index: Int) extends QuestionPage[String] {

  override def path: JsPath = JsPath \ TaskCategory.TransferDetails.toString \ TypeOfAsset.Property.entryName \ index \ toString

  override def toString: String = PropertyEntry.PropDescription

  override protected def nextPageNormalMode(answers: UserAnswers): Call =
    AssetsMiniJourneysRoutes.PropertyCYAController.onPageLoad(NormalMode, index)

  override protected def nextPageCheckMode(answers: UserAnswers): Call =
    AssetsMiniJourneysRoutes.PropertyCYAController.onPageLoad(CheckMode, index)

  override protected def nextPageFinalCheckMode(answers: UserAnswers): Call =
    AssetsMiniJourneysRoutes.PropertyCYAController.onPageLoad(FinalCheckMode, index)

  override protected def nextPageAmendCheckMode(answers: UserAnswers): Call =
    AssetsMiniJourneysRoutes.PropertyCYAController.onPageLoad(AmendCheckMode, index)

  final def changeLink(mode: Mode): Call =
    AssetsMiniJourneysRoutes.PropertyDescriptionController.onPageLoad(mode, index)
}

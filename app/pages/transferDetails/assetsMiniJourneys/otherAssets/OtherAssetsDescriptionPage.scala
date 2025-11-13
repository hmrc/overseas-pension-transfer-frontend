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

package pages.transferDetails.assetsMiniJourneys.otherAssets

import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import models.assets.{OtherAssetsEntry, TypeOfAsset}
import models.{Mode, TaskCategory, UserAnswers}
import pages.{MiniJourneyNextPage, QuestionPage}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class OtherAssetsDescriptionPage(index: Int) extends QuestionPage[String] with MiniJourneyNextPage {

  override def path: JsPath = JsPath \ TaskCategory.TransferDetails.toString \ TypeOfAsset.Other.entryName \ index \ toString

  override def toString: String = OtherAssetsEntry.AssetDescription

  override def decideNextPage(answers: UserAnswers, mode: Mode): Call = {
    answers.get(OtherAssetsValuePage(index)) match {
      case Some(_) => AssetsMiniJourneysRoutes.OtherAssetsCYAController.onPageLoad(mode, index)
      case None    => AssetsMiniJourneysRoutes.OtherAssetsValueController.onPageLoad(mode, index)
    }
  }

  final def changeLink(mode: Mode): Call =
    AssetsMiniJourneysRoutes.OtherAssetsDescriptionController.onPageLoad(mode, index)
}

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
import controllers.transferDetails.routes
import models.address._
import models.assets.{PropertyEntry, TypeOfAsset}
import models.{AmendCheckMode, CheckMode, FinalCheckMode, Mode, NormalMode, SessionData, TaskCategory, UserAnswers}
import pages.transferDetails.assetsMiniJourneys.otherAssets.OtherAssetsValuePage
import pages.{NextPageWith, QuestionPage}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class PropertyAddressPage(index: Int) extends QuestionPage[PropertyAddress] {

  override def path: JsPath = JsPath \ TaskCategory.TransferDetails.toString \ TypeOfAsset.Property.entryName \ index \ toString

  override def toString: String = PropertyEntry.PropertyAddress

  override protected def nextPageNormalMode(answers: UserAnswers): Call =
    AssetsMiniJourneysRoutes.PropertyValueController.onPageLoad(NormalMode, index)

  override protected def nextPageCheckMode(answers: UserAnswers): Call = decideNextPage(answers, CheckMode)

  override protected def nextPageFinalCheckMode(answers: UserAnswers): Call = decideNextPage(answers, FinalCheckMode)

  override protected def nextPageAmendCheckMode(answers: UserAnswers): Call = decideNextPage(answers, AmendCheckMode)

  private def decideNextPage(answers: UserAnswers, mode: Mode): Call = {
    answers.get(PropertyValuePage(index)) match {
      case Some(_) => AssetsMiniJourneysRoutes.PropertyCYAController.onPageLoad(mode, index)
      case None    => AssetsMiniJourneysRoutes.PropertyValueController.onPageLoad(mode, index)
    }
  }

  final def changeLink(mode: Mode): Call =
    AssetsMiniJourneysRoutes.PropertyAddressController.onPageLoad(mode, index)
}

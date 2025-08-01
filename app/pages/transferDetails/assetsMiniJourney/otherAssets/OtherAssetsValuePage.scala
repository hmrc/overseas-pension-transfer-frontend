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

package pages.transferDetails.assetsMiniJourney.otherAssets

import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import controllers.transferDetails.routes
import models.{OtherAssetsEntry, TaskCategory, TypeOfAsset, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class OtherAssetsValuePage(index: Int) extends QuestionPage[BigDecimal] {

  override def path: JsPath = JsPath \ TaskCategory.TransferDetails.toString \ TypeOfAsset.Other.toString \ index \ toString

  override def toString: String = OtherAssetsEntry.OtherAssetsValue

  override protected def nextPageNormalMode(answers: UserAnswers): Call =
    controllers.routes.IndexController.onPageLoad() // need custom cya controller

  override protected def nextPageCheckMode(answers: UserAnswers): Call =
    routes.TransferDetailsCYAController.onPageLoad() // index pased into correct controller

  final def changeLink(answers: UserAnswers): Call =
    AssetsMiniJourneysRoutes.OtherAssetsValueController.onPageLoad(index)
}

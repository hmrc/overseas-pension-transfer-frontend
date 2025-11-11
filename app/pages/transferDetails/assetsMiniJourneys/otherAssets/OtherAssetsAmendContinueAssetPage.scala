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
import models.assets.{AssetsMiniJourneyRegistry, TypeOfAsset}
import models.{Mode, UserAnswers}
import navigators.TypeOfAssetNavigator
import pages.transferDetails.assetsMiniJourneys.AmendContinueContext
import pages.{MiniJourneyNextAssetPage, QuestionPage}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object OtherAssetsAmendContinueAssetPage extends QuestionPage[Boolean] with MiniJourneyNextAssetPage[AmendContinueContext] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "otherAssetsAmendContinue"

  override def decideNextPage(answers: UserAnswers, sessionDataWithIndex: AmendContinueContext, mode: Mode, modeCall: Call): Call = {
    val (sessionData, nextIndex) = sessionDataWithIndex
    answers.get(OtherAssetsAmendContinueAssetPage) match {
      case Some(true)  => AssetsMiniJourneyRegistry.forType(TypeOfAsset.Other).get.call(mode, Some(nextIndex))
      case Some(false) => super.nextAsset(sessionData, mode, modeCall)
      case _           => controllers.routes.JourneyRecoveryController.onPageLoad()
    }
  }
}

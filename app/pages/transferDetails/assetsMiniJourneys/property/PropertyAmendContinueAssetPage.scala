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

import models.assets.{AssetsMiniJourneyRegistry, TypeOfAsset}
import models.{Mode, UserAnswers}
import pages.transferDetails.assetsMiniJourneys.{AmendContinueContext, NextAssetMiniJourney}
import pages.{MiniJourneyNextPageWith, QuestionPage}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object PropertyAmendContinueAssetPage extends QuestionPage[Boolean] with MiniJourneyNextPageWith[AmendContinueContext] with NextAssetMiniJourney {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "propertyAmendContinue"

  override def decideNextPage(answers: UserAnswers, sessionDataWithIndex: AmendContinueContext, mode: Mode, modeCall: Call): Call = {
    val (sessionData, nextIndex) = sessionDataWithIndex
    answers.get(PropertyAmendContinueAssetPage) match {
      case Some(true)  => AssetsMiniJourneyRegistry.startOf(TypeOfAsset.Property, mode, nextIndex)
      case Some(false) => getNextAsset(sessionData, mode, modeCall)
      case _           => controllers.routes.JourneyRecoveryController.onPageLoad()
    }
  }
}

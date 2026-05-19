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

package pages.transferDetails

import play.api.mvc.Call
import controllers.transferDetails.routes
import pages.MiniJourneyNextPageWith
import pages.QuestionPage
import pages.transferDetails.assetsMiniJourneys.NextAssetMiniJourney
import models._
import play.api.libs.json.JsPath
import models.assets.TypeOfAsset

case object TypeOfAssetPage
    extends QuestionPage[Seq[TypeOfAsset]]
    with MiniJourneyNextPageWith[SessionData]
    with NextAssetMiniJourney {

  override def path: JsPath = JsPath \ TaskCategory.TransferDetails.toString \ toString

  override def toString: String = "typeOfAsset"

  override def decideNextPage(answers: UserAnswers, sessionData: SessionData, mode: Mode, modeCall: Call): Call =
    getNextAsset(sessionData, mode, modeCall)

  final def changeLink(mode: Mode): Call =
    routes.TypeOfAssetController.onPageLoad(mode)
}

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

import models.{Mode, SessionData, UserAnswers}
import pages.transferDetails.assetsMiniJourneys.NextAssetMiniJourney
import pages.{MiniJourneyNextPageWith, QuestionPage}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object MoreQuotedSharesDeclarationPage extends QuestionPage[Boolean] with MiniJourneyNextPageWith[SessionData] with NextAssetMiniJourney {

  override def path: JsPath =
    JsPath \ "transferDetails" \ "moreQuoted"

  override def decideNextPage(answers: UserAnswers, sessionData: SessionData, mode: Mode, modeCall: Call): Call =
    getNextAsset(sessionData, mode, modeCall)
}

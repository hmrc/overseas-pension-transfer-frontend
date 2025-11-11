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

package pages.transferDetails.assetsMiniJourneys.cash

import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import controllers.transferDetails.routes
import models.assets.CashEntry
import models.{AmendCheckMode, CheckMode, FinalCheckMode, Mode, NormalMode, SessionData, TaskCategory, UserAnswers}
import navigators.TypeOfAssetNavigator
import pages.{MiniJourneyNextAssetPage, NextPageWith, QuestionPage}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object CashAmountInTransferPage extends QuestionPage[BigDecimal] with MiniJourneyNextAssetPage[SessionData] {

  override def path: JsPath = JsPath \ TaskCategory.TransferDetails.toString \ toString

  override def toString: String = CashEntry.CashValue

  override def decideNextPage(userAnswers: UserAnswers, sessionData: SessionData, mode: Mode, modeCall: Call): Call =
    super.nextAsset(sessionData, mode, modeCall)

  final def changeLink(mode: Mode): Call =
    AssetsMiniJourneysRoutes.CashAmountInTransferController.onPageLoad(mode)
}

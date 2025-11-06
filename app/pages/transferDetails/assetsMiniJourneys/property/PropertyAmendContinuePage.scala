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
import models.{AmendCheckMode, CheckMode, FinalCheckMode, Mode, NormalMode, SessionData, UserAnswers}
import navigators.TypeOfAssetNavigator
import pages.transferDetails.assetsMiniJourneys.otherAssets.OtherAssetsAmendContinuePage
import pages.{MiniJourneyNextPage, NextPageWith, QuestionPage}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object PropertyAmendContinuePage extends QuestionPage[Boolean] with NextPageWith[(SessionData, Int)] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "propertyAmendContinue"

  override protected def nextPageWith(answers: UserAnswers, sessionDataWithIndex: (SessionData, Int)): Call =
    decideNextPage(answers, sessionDataWithIndex, NormalMode, routes.TransferDetailsCYAController.onPageLoad())

  override protected def nextPageCheckModeWith(answers: UserAnswers, sessionDataWithIndex: (SessionData, Int)): Call =
    decideNextPage(answers, sessionDataWithIndex, CheckMode, routes.TransferDetailsCYAController.onPageLoad())

  override protected def nextPageFinalCheckModeWith(answers: UserAnswers, sessionDataWithIndex: (SessionData, Int)): Call =
    decideNextPage(answers, sessionDataWithIndex, FinalCheckMode, super.nextPageFinalCheckMode(answers))

  override protected def nextPageAmendCheckModeWith(answers: UserAnswers, sessionDataWithIndex: (SessionData, Int)): Call =
    decideNextPage(answers, sessionDataWithIndex, AmendCheckMode, super.nextPageAmendCheckMode(answers))

  private def decideNextPage(answers: UserAnswers, sessionDataWithIndex: (SessionData, Int), mode: Mode, modeCall: Call): Call = {
    val (sessionData, nextIndex) = sessionDataWithIndex
    answers.get(PropertyAmendContinuePage) match {
      case Some(true)  => AssetsMiniJourneysRoutes.PropertyAddressController.onPageLoad(mode, nextIndex)
      case Some(false) => TypeOfAssetNavigator.getNextAssetRoute(sessionData) match {
          case Some(route) => route
          case None        => modeCall
        }
      case _           => controllers.routes.JourneyRecoveryController.onPageLoad()
    }
  }
}

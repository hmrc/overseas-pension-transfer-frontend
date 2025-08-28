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

package pages.transferDetails.assetsMiniJourneys.unquotedShares

import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import controllers.transferDetails.routes
import models.{NormalMode, UserAnswers}
import navigators.TypeOfAssetNavigator
import pages.{NextPageWith, QuestionPage}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object UnquotedSharesAmendContinuePage extends QuestionPage[Boolean] with NextPageWith[Int] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "unquotedSharesAmendContinue"

  override protected def nextPageWith(answers: UserAnswers, nextIndex: Int): Call = {
    answers.get(UnquotedSharesAmendContinuePage) match {
      case Some(true)  => AssetsMiniJourneysRoutes.UnquotedSharesCompanyNameController.onPageLoad(NormalMode, nextIndex)
      case Some(false) => TypeOfAssetNavigator.getNextAssetRoute(answers) match {
          case Some(route) => route
          case None        => routes.TransferDetailsCYAController.onPageLoad()
        }
      case _           => controllers.routes.JourneyRecoveryController.onPageLoad()
    }
  }

  override protected def nextPageCheckMode(answers: UserAnswers): Call =
    routes.TransferDetailsCYAController.onPageLoad()
}

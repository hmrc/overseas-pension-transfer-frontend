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
import handlers.AssetThresholdHandler
import models.assets.TypeOfAsset
import models.{AmendCheckMode, CheckMode, FinalCheckMode, Mode, NormalMode, UserAnswers}
import pages.Page
import play.api.mvc.Call

case class UnquotedSharesCYAPage(index: Int) extends Page {

  override protected def nextPageNormalMode(answers: UserAnswers): Call =
    decideNextPage(answers, NormalMode)

  override protected def nextPageCheckMode(answers: UserAnswers): Call =
    decideNextPage(answers, CheckMode)

  override protected def nextPageFinalCheckMode(answers: UserAnswers): Call =
    decideNextPage(answers, FinalCheckMode)

  override protected def nextPageAmendCheckMode(answers: UserAnswers): Call =
    decideNextPage(answers, AmendCheckMode)

  private def decideNextPage(answers: UserAnswers, mode: Mode): Call = {
    val unquotedSharesCount = AssetThresholdHandler.getAssetCount(answers, TypeOfAsset.UnquotedShares)
    if (unquotedSharesCount >= 5) {
      controllers.transferDetails.assetsMiniJourneys.unquotedShares.routes.MoreUnquotedSharesDeclarationController.onPageLoad(mode = mode)
    } else {
      AssetsMiniJourneysRoutes.UnquotedSharesAmendContinueController.onPageLoad(mode = mode)
    }
  }

  final def changeLink(mode: Mode): Call =
    AssetsMiniJourneysRoutes.UnquotedSharesCompanyNameController.onPageLoad(mode, index)
}

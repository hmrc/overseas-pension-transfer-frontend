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

package pages

import controllers.transferDetails.routes
import models.assets.AssetsMiniJourneyRegistry
import models.{AmendCheckMode, CheckMode, FinalCheckMode, Mode, NormalMode, SessionData, UserAnswers}
import play.api.mvc.Call

trait MiniJourneyNextAssetPage[A] extends NextPageWith[A] { self: Page =>

  protected def decideNextPage(answers: UserAnswers, ctx: A, mode: Mode, modeCall: Call): Call

  override protected def nextPageWith(answers: UserAnswers, ctx: A): Call =
    decideNextPage(answers, ctx, NormalMode, routes.TransferDetailsCYAController.onPageLoad())

  override protected def nextPageCheckModeWith(answers: UserAnswers, ctx: A): Call =
    decideNextPage(answers, ctx, CheckMode, routes.TransferDetailsCYAController.onPageLoad())

  override protected def nextPageFinalCheckModeWith(answers: UserAnswers, ctx: A): Call =
    decideNextPage(answers, ctx, FinalCheckMode, self.nextPageFinalCheckMode(answers))

  override protected def nextPageAmendCheckModeWith(answers: UserAnswers, ctx: A): Call =
    decideNextPage(answers, ctx, AmendCheckMode, self.nextPageAmendCheckMode(answers))

  protected def nextAsset(sessionData: SessionData, mode: Mode, modeCall: Call): Call = {
    val nextAsset = AssetsMiniJourneyRegistry.firstIncompleteJourney(sessionData).map(_.call(mode))
    nextAsset match {
      case Some(route) => route
      case None        => modeCall
    }
  }
}

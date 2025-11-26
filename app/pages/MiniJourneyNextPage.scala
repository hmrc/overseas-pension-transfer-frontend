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

import models.{AmendCheckMode, CheckMode, FinalCheckMode, Mode, NormalMode, UserAnswers}
import play.api.mvc.Call

trait MiniJourneyNextPage extends Page { self: Page =>

  protected def decideNextPage(answers: UserAnswers, mode: Mode): Call

  override protected def nextPageNormalMode(answers: UserAnswers): Call =
    decideNextPage(answers, NormalMode)

  override protected def nextPageCheckMode(answers: UserAnswers): Call =
    decideNextPage(answers, CheckMode)

  override protected def nextPageFinalCheckMode(answers: UserAnswers): Call =
    decideNextPage(answers, FinalCheckMode)

  override protected def nextPageAmendCheckMode(answers: UserAnswers): Call =
    decideNextPage(answers, AmendCheckMode)
}

trait MiniJourneyNextPageWith[A] extends NextPageWith[A] { self: Page =>

  protected def decideNextPage(answers: UserAnswers, ctx: A, mode: Mode, modeCall: Call): Call

  // This can be overriden if necessary with other mode calls
  protected def getModeCall(mode: Mode, answers: UserAnswers): Call =
    mode match {
      case NormalMode | CheckMode =>
        controllers.transferDetails.routes.TransferDetailsCYAController.onPageLoad()
      case FinalCheckMode         =>
        self.nextPageFinalCheckMode(answers)
      case AmendCheckMode         =>
        self.nextPageAmendCheckMode(answers)
    }

  override protected def nextPageWith(answers: UserAnswers, ctx: A): Call =
    decideNextPage(answers, ctx, NormalMode, getModeCall(NormalMode, answers))

  override protected def nextPageCheckModeWith(answers: UserAnswers, ctx: A): Call =
    decideNextPage(answers, ctx, CheckMode, getModeCall(CheckMode, answers))

  override protected def nextPageFinalCheckModeWith(answers: UserAnswers, ctx: A): Call =
    decideNextPage(answers, ctx, FinalCheckMode, getModeCall(FinalCheckMode, answers))

  override protected def nextPageAmendCheckModeWith(answers: UserAnswers, ctx: A): Call =
    decideNextPage(answers, ctx, AmendCheckMode, getModeCall(AmendCheckMode, answers))
}

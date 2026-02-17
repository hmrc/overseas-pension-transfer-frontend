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

import controllers.routes
import models.QtStatus.AmendInProgress
import models.{NormalMode, UserAnswers}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object DiscardTransferConfirmPage extends QuestionPage[Boolean] with NextPageWith[Option[String]] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "discardTransferConfirm"

  override protected def nextPageNormalMode(answers: UserAnswers): Call = {
    nextPageWith(answers)
  }

  override protected def nextPageAmendCheckMode(answers: UserAnswers): Call = {
    nextPageAmendCheckModeWith(answers)
  }

  override protected def nextPageWith(answers: UserAnswers, context: Option[String] = None): Call = {
    answers.get(DiscardTransferConfirmPage) match {
      case Some(true)  => routes.DashboardController.onPageLoad()
      case Some(false) => routes.TaskListController.onPageLoad()
      case _           => routes.JourneyRecoveryController.onPageLoad()
    }
  }

  override protected def nextPageAmendCheckModeWith(answers: UserAnswers, context: Option[String] = None): Call = {
    answers.get(DiscardTransferConfirmPage) match {
      case Some(true)                      => routes.DashboardController.onPageLoad()
      case Some(false) if context.nonEmpty =>
        controllers.viewandamend.routes.ViewAmendSubmittedController.fromDraft(
          answers.id,
          answers.pstr,
          AmendInProgress,
          context.get
        )
      case _                               => routes.JourneyRecoveryController.onPageLoad()
    }
  }
}

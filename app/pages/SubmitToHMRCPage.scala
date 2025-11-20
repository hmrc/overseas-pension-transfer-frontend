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
import models.authentication.{AuthenticatedUser, Psa, Psp}
import models.{AmendCheckMode, NormalMode, UserAnswers}
import play.api.Logging
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object SubmitToHMRCPage
    extends QuestionPage[Boolean]
    with NextPageWith[AuthenticatedUser] with Logging {

  override def path: JsPath     = JsPath \ toString
  override def toString: String = "submitToHMRC"

  override protected def nextPageAmendCheckModeWith(answers: UserAnswers, authenticatedUser: AuthenticatedUser): Call = {
    answers.get(SubmitToHMRCPage) match {
      case Some(true)  =>
        authenticatedUser.userType match {
          case Psa => routes.PsaDeclarationController.onPageLoad(AmendCheckMode)
          case Psp => routes.PspDeclarationController.onPageLoad(AmendCheckMode)
          case _   => routes.JourneyRecoveryController.onPageLoad()
        }
      case Some(false) =>
        routes.DashboardController.onPageLoad()
      case _           => routes.JourneyRecoveryController.onPageLoad()
    }
  }

  override protected def nextPageWith(answers: UserAnswers, authenticatedUser: AuthenticatedUser): Call = {
    answers.get(SubmitToHMRCPage) match {
      case Some(true)  =>
        authenticatedUser.userType match {
          case Psa => routes.PsaDeclarationController.onPageLoad(NormalMode)
          case Psp => routes.PspDeclarationController.onPageLoad(NormalMode)
          case _   => routes.JourneyRecoveryController.onPageLoad()
        }
      case Some(false) =>
        routes.DashboardController.onPageLoad()
      case _           => routes.JourneyRecoveryController.onPageLoad()
    }
  }
}

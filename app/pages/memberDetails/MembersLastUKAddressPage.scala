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

package pages.memberDetails

import controllers.memberDetails.routes
import models.address._
import models.{AmendCheckMode, CheckMode, FinalCheckMode, Mode, NormalMode, TaskCategory, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object MembersLastUKAddressPage extends QuestionPage[MembersLastUKAddress] {

  override def path: JsPath = JsPath \ TaskCategory.MemberDetails.toString \ toString

  override def toString: String = "lastPrincipalAddDetails"

  override protected def nextPageNormalMode(answers: UserAnswers): Call =
    routes.MemberDateOfLeavingUKController.onPageLoad(NormalMode)

  override protected def nextPageCheckMode(answers: UserAnswers): Call =
    answers.get(MemberDateOfLeavingUKPage) match {
      case Some(_) => routes.MemberDetailsCYAController.onPageLoad()
      case None    => routes.MemberDateOfLeavingUKController.onPageLoad(CheckMode)
      case _       => controllers.routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def nextPageFinalCheckMode(answers: UserAnswers): Call =
    answers.get(MemberDateOfLeavingUKPage) match {
      case Some(_) => controllers.checkYourAnswers.routes.CheckYourAnswersController.onPageLoad()
      case None    => routes.MemberDateOfLeavingUKController.onPageLoad(FinalCheckMode)
      case _       => controllers.routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def nextPageAmendCheckMode(answers: UserAnswers): Call =
    answers.get(MemberDateOfLeavingUKPage) match {
      case Some(_) => controllers.viewandamend.routes.ViewAmendSubmittedController.amend()
      case None    => routes.MemberDateOfLeavingUKController.onPageLoad(AmendCheckMode)
      case _       => controllers.routes.JourneyRecoveryController.onPageLoad()
    }

  final def changeLink(mode: Mode): Call =
    routes.MembersLastUKAddressController.onPageLoad(mode)
}

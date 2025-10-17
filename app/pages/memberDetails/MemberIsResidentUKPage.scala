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

import models.{AmendCheckMode, CheckMode, Mode, NormalMode, TaskCategory, UserAnswers}
import controllers.{memberDetails, routes}
import pages.QuestionPage
import play.api.Logging
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case object MemberIsResidentUKPage extends QuestionPage[Boolean] with Logging {

  override def path: JsPath = JsPath \ TaskCategory.MemberDetails.toString \ toString

  override def toString: String = "memUkResident"

  override protected def nextPageNormalMode(answers: UserAnswers): Call =
    answers.get(MemberIsResidentUKPage) match {
      case Some(false) => memberDetails.routes.MemberHasEverBeenResidentUKController.onPageLoad(NormalMode)
      case Some(true)  => memberDetails.routes.MemberDetailsCYAController.onPageLoad()
      case _           => routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def nextPageCheckMode(answers: UserAnswers): Call =
    answers.get(MemberIsResidentUKPage) match {
      case Some(false) => memberDetails.routes.MemberHasEverBeenResidentUKController.onPageLoad(CheckMode)
      case Some(true)  => memberDetails.routes.MemberDetailsCYAController.onPageLoad()
      case _           => routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def nextPageAmendCheckMode(answers: UserAnswers): Call =
    answers.get(MemberIsResidentUKPage) match {
      case Some(false) => memberDetails.routes.MemberHasEverBeenResidentUKController.onPageLoad(AmendCheckMode)
      case Some(true)  => super.nextPageAmendCheckMode(answers)
      case _           => routes.JourneyRecoveryController.onPageLoad()
    }

  final def changeLink(mode: Mode): Call =
    memberDetails.routes.MemberIsResidentUKController.onPageLoad(mode)

  override def cleanup(maybeIsResidentUk: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] = {
    maybeIsResidentUk match {
      case Some(true) => userAnswers
          .remove(MemberHasEverBeenResidentUKPage)
          .flatMap(_.remove(MembersLastUKAddressPage))
          .flatMap(_.remove(MemberDateOfLeavingUKPage))
      case _          => super.cleanup(maybeIsResidentUk, userAnswers)
    }
  }
}

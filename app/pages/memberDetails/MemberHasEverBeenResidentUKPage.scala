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

import controllers.{memberDetails, routes}
import models.{CheckMode, Mode, NormalMode, TaskCategory, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case object MemberHasEverBeenResidentUKPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ TaskCategory.MemberDetails.toString \ toString

  override def toString: String = "memEverUkResident"

  override protected def nextPageNormalMode(answers: UserAnswers): Call =
    answers.get(MemberHasEverBeenResidentUKPage) match {
      case Some(true)  => memberDetails.routes.MembersLastUkAddressLookupController.onPageLoad(NormalMode)
      case Some(false) => memberDetails.routes.MemberDetailsCYAController.onPageLoad()
      case _           => routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def nextPageCheckMode(answers: UserAnswers): Call =
    answers.get(MemberHasEverBeenResidentUKPage) match {
      case Some(true)  => memberDetails.routes.MembersLastUkAddressLookupController.onPageLoad(CheckMode)
      case Some(false) => memberDetails.routes.MemberDetailsCYAController.onPageLoad()
      case _           => routes.JourneyRecoveryController.onPageLoad()
    }

  final def changeLink(mode: Mode): Call =
    memberDetails.routes.MemberHasEverBeenResidentUKController.onPageLoad(mode)

  override def cleanup(maybeHasEverBeenResidentUk: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] = {
    maybeHasEverBeenResidentUk match {
      case Some(false) => userAnswers
          .remove(MembersLastUKAddressPage)
          .flatMap(_.remove(MemberDateOfLeavingUKPage))
      case _           => super.cleanup(maybeHasEverBeenResidentUk, userAnswers)
    }
  }
}

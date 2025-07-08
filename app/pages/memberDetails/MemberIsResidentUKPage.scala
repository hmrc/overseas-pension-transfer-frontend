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

import models.{CheckMode, NormalMode, TaskCategory, UserAnswers}
import controllers.{memberDetails, routes}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object MemberIsResidentUKPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ TaskCategory.MemberDetails.toString \ toString

  override def toString: String = "memUkResident"

  override protected def nextPageNormalMode(answers: UserAnswers): Call =
    answers.get(MemberIsResidentUKPage) match {
      case Some(false) => memberDetails.routes.MemberHasEverBeenResidentUKController.onPageLoad(NormalMode)
      case Some(true)  => memberDetails.routes.MemberDetailsCYAController.onPageLoad()
      case _           => routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def nextPageCheckMode(answers: UserAnswers): Call =
    memberDetails.routes.MemberDetailsCYAController.onPageLoad()

  final def changeLink(answers: UserAnswers): Call =
    memberDetails.routes.MemberIsResidentUKController.onPageLoad(CheckMode)
}

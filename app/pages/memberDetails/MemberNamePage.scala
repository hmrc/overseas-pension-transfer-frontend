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
import models.{Mode, NormalMode, PersonName, TaskCategory, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object MemberNamePage extends QuestionPage[PersonName] {

  override def path: JsPath = JsPath \ TaskCategory.MemberDetails.toString \ toString

  override def toString: String = "name"

  override protected def nextPageNormalMode(answers: UserAnswers): Call = {
    answers.get(MemberNinoPage) match {
      case Some(_) => routes.MemberNinoController.onPageLoad(NormalMode)
      case None    => answers.get(MemberDoesNotHaveNinoPage) match {
          case Some(_) => routes.MemberDoesNotHaveNinoController.onPageLoad(NormalMode)
          case None    => routes.MemberNinoController.onPageLoad(NormalMode)
        }
    }

  }

  override protected def nextPageCheckMode(answers: UserAnswers): Call =
    routes.MemberDetailsCYAController.onPageLoad()

  final def changeLink(mode: Mode): Call =
    routes.MemberNameController.onPageLoad(mode)
}

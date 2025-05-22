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
import models.{CheckMode, NormalMode, PersonName, TaskCategory, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call
import utils.UserAnswersOps._
import scala.util.{Success, Try}

case object MemberNamePage extends QuestionPage[PersonName] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "memberName"

  override protected def nextPageNormalMode(answers: UserAnswers): Call =
    routes.MemberNinoController.onPageLoad(NormalMode)

  override def cleanup(value: Option[PersonName], userAnswers: UserAnswers): Try[UserAnswers] =
    userAnswers.syncToFormData(value)(this)(name => _.updateMemberDetailsOrCreate(_.copy(memberName = Some(name))))(userAnswers)

  override def read(userAnswers: UserAnswers): Option[PersonName] =
    userAnswers.get(_.memberDetails.flatMap(_.memberName))

  override protected def nextPageCheckMode(answers: UserAnswers): Call =
    routes.MemberDetailsCYAController.onPageLoad()

  final def changeLink(answers: UserAnswers): Call =
    routes.MemberNameController.onPageLoad(CheckMode)
}

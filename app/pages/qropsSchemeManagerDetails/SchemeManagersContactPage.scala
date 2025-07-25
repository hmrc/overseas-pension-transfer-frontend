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

package pages.qropsSchemeManagerDetails

import controllers.qropsSchemeManagerDetails.routes
import models.{CheckMode, TaskCategory, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object SchemeManagersContactPage extends QuestionPage[String] {

  override def path: JsPath = JsPath \ TaskCategory.SchemeManagerDetails.toString \ toString

  override def toString: String = "schemeManagerPhone"

  override protected def nextPageNormalMode(answers: UserAnswers): Call =
    routes.SchemeManagerDetailsCYAController.onPageLoad()

  override protected def nextPageCheckMode(answers: UserAnswers): Call =
    routes.SchemeManagerDetailsCYAController.onPageLoad()

  final def changeLink(answers: UserAnswers): Call =
    routes.SchemeManagersContactController.onPageLoad(CheckMode)
}

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

package pages.qropsDetails

import controllers.routes
import models.{PersonName, TaskCategory, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object QROPSDetailsSummaryPage extends QuestionPage[PersonName] {

  override def path: JsPath = JsPath \ TaskCategory.QROPSDetails.toString \ toString

  override def toString: String = "qropsDetailsSummary"

  override protected def nextPageNormalMode(answers: UserAnswers): Call =
    routes.IndexController.onPageLoad() // TODO This will have to be changed to 'overseas transfer report' main page when exists

  override protected def nextPageCheckMode(answers: UserAnswers): Call =
    routes.JourneyRecoveryController.onPageLoad()
}

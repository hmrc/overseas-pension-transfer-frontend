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

package pages.transferDetails

import controllers.transferDetails.routes
import models.{CheckMode, TaskCategory, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object OverseasTransferAllowancePage extends QuestionPage[BigDecimal] {

  override def path: JsPath = JsPath \ TaskCategory.TransferDetails.toString \ toString

  override def toString: String = "allowanceBeforeTransfer"

  override protected def nextPageNormalMode(answers: UserAnswers): Call =
    controllers.routes.IndexController.onPageLoad() // TODO change while connecting the pages

  override protected def nextPageCheckMode(answers: UserAnswers): Call =
    routes.TransferDetailsCYAController.onPageLoad()

  final def changeLink(answers: UserAnswers): Call =
    routes.OverseasTransferAllowanceController.onPageLoad(CheckMode)
}

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
import models.{AmendCheckMode, ApplicableTaxExclusions, CheckMode, FinalCheckMode, Mode, NormalMode, TaskCategory, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object ApplicableTaxExclusionsPage extends QuestionPage[Set[ApplicableTaxExclusions]] {

  override def path: JsPath = JsPath \ TaskCategory.TransferDetails.toString \ toString

  override def toString: String = "applicableExclusion"

  private def nextPage(mode: Mode): Call =
    routes.AmountOfTaxDeductedController.onPageLoad(mode)

  override protected def nextPageNormalMode(answers: UserAnswers): Call =
    nextPage(NormalMode)

  override protected def nextPageCheckMode(answers: UserAnswers): Call =
    nextPage(CheckMode)

  override protected def nextPageFinalCheckMode(answers: UserAnswers): Call =
    nextPage(FinalCheckMode)

  override protected def nextPageAmendCheckMode(answers: UserAnswers): Call =
    nextPage(AmendCheckMode)

  final def changeLink(mode: Mode): Call =
    routes.ApplicableTaxExclusionsController.onPageLoad(mode)
}

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
import models.WhyTransferIsTaxable.{NoExclusion, TransferExceedsOTCAllowance}
import models.{AmendCheckMode, CheckMode, FinalCheckMode, Mode, NormalMode, TaskCategory, UserAnswers, WhyTransferIsTaxable}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case object WhyTransferIsTaxablePage extends QuestionPage[WhyTransferIsTaxable] {

  override def path: JsPath = JsPath \ TaskCategory.TransferDetails.toString \ toString

  override def toString: String = "whyTaxableOT"

  private def nextPageBase(answers: UserAnswers, mode: Mode): Call =
    answers.get(WhyTransferIsTaxablePage) match {
      case Some(TransferExceedsOTCAllowance) => routes.ApplicableTaxExclusionsController.onPageLoad(mode)
      case Some(NoExclusion)                 => routes.AmountOfTaxDeductedController.onPageLoad(mode)
      case _                                 => controllers.routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def nextPageNormalMode(answers: UserAnswers): Call =
    nextPageBase(answers, NormalMode)

  override protected def nextPageCheckMode(answers: UserAnswers): Call =
    nextPageBase(answers, CheckMode)

  override protected def nextPageFinalCheckMode(answers: UserAnswers): Call =
    nextPageBase(answers, FinalCheckMode)

  override protected def nextPageAmendCheckMode(answers: UserAnswers): Call =
    nextPageBase(answers, AmendCheckMode)

  final def changeLink(mode: Mode): Call =
    routes.WhyTransferIsTaxableController.onPageLoad(mode)

  override def cleanup(maybeExclusion: Option[WhyTransferIsTaxable], userAnswers: UserAnswers): Try[UserAnswers] = {
    maybeExclusion match {
      case Some(NoExclusion) =>
        userAnswers.remove(ApplicableTaxExclusionsPage)
      case _                 => super.cleanup(maybeExclusion, userAnswers)
    }
  }

}

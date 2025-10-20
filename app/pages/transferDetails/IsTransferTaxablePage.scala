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
import models.{AmendCheckMode, CheckMode, FinalCheckMode, Mode, NormalMode, TaskCategory, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case object IsTransferTaxablePage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ TaskCategory.TransferDetails.toString \ toString

  override def toString: String = "paymentTaxableOverseas"

  private def nextPage(answers: UserAnswers, mode: Mode): Call =
    answers.get(IsTransferTaxablePage) match {
      case Some(true)  => routes.WhyTransferIsTaxableController.onPageLoad(mode)
      case Some(false) => routes.WhyTransferIsNotTaxableController.onPageLoad(mode)
      case _           => controllers.routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def nextPageNormalMode(answers: UserAnswers): Call =
    nextPage(answers, NormalMode)

  override protected def nextPageCheckMode(answers: UserAnswers): Call =
    nextPage(answers, CheckMode)

  override protected def nextPageFinalCheckMode(answers: UserAnswers): Call =
    nextPage(answers, FinalCheckMode)

  override protected def nextPageAmendCheckMode(answers: UserAnswers): Call =
    nextPage(answers, AmendCheckMode)

  final def changeLink(mode: Mode): Call =
    routes.IsTransferTaxableController.onPageLoad(mode)

  override def cleanup(maybeTransferIsTaxable: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] = {
    maybeTransferIsTaxable match {
      case Some(false) =>
        for {
          ua1 <- userAnswers.remove(WhyTransferIsTaxablePage)
          ua2 <- ua1.remove(ApplicableTaxExclusionsPage)
          ua3 <- ua2.remove(AmountOfTaxDeductedPage)
          ua4 <- ua3.remove(NetTransferAmountPage)
        } yield ua4
      case Some(true)  =>
        userAnswers.remove(WhyTransferIsNotTaxablePage)
      case _           => super.cleanup(maybeTransferIsTaxable, userAnswers)
    }
  }
}

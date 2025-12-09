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

package viewmodels.checkAnswers.transferDetails

import models.{Mode, UserAnswers}
import pages.transferDetails.OverseasTransferAllowancePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.CurrencyFormats.currencyFormat
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object OverseasTransferAllowanceSummary {

  def row(mode: Mode, answers: UserAnswers, showChangeLink: Boolean = true)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(OverseasTransferAllowancePage).map { answer =>
      val actions =
        if (showChangeLink) {
          Seq(
            ActionItemViewModel("site.change", OverseasTransferAllowancePage.changeLink(mode).url)
              .withVisuallyHiddenText(messages("overseasTransferAllowance.change.hidden"))
          )
        } else {
          Seq.empty
        }

      SummaryListRowViewModel(
        key     = "overseasTransferAllowance.checkYourAnswersLabel",
        value   = ValueViewModel(currencyFormat(answer)),
        actions = actions
      )
    }
}

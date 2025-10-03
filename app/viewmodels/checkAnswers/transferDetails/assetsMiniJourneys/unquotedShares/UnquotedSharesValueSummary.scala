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

package viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.unquotedShares

import models.{Mode, SessionData, UserAnswers}
import pages.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesValuePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.CurrencyFormats.currencyFormat
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object UnquotedSharesValueSummary {

  def row(mode: Mode, userAnswers: UserAnswers, index: Int)(implicit messages: Messages): Option[SummaryListRow] =
    userAnswers.get(UnquotedSharesValuePage(index)).map {
      answer =>
        SummaryListRowViewModel(
          key     = "unquotedSharesValue.checkYourAnswersLabel",
          value   = ValueViewModel(currencyFormat(answer)),
          actions = Seq(
            ActionItemViewModel("site.change", UnquotedSharesValuePage(index).changeLink(mode).url)
              .withVisuallyHiddenText(messages("unquotedSharesValue.change.hidden"))
          )
        )
    }
}

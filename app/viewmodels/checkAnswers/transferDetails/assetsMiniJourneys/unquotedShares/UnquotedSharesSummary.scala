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
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

case object UnquotedSharesSummary {

  def rows(mode: Mode, sessionData: SessionData, index: Int)(implicit messages: Messages): Seq[SummaryListRow] = {
    val companyNameRow: Option[SummaryListRow]   = UnquotedSharesCompanyNameSummary.row(mode, sessionData, index)
    val valueRow: Option[SummaryListRow]         = UnquotedSharesValueSummary.row(mode, sessionData, index)
    val quantityRow: Option[SummaryListRow]      = UnquotedSharesNumberSummary.row(mode, sessionData, index)
    val classOfSharesRow: Option[SummaryListRow] = UnquotedSharesClassSummary.row(mode, sessionData, index)

    Seq(companyNameRow, valueRow, quantityRow, classOfSharesRow).flatten
  }
}

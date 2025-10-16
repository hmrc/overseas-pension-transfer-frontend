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

package viewmodels.checkAnswers.qropsDetails

import models.{Mode, UserAnswers}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

case object QROPSDetailsSummary {

  def rows(mode: Mode, userAnswers: UserAnswers, showChangeLinks: Boolean = true)(implicit messages: Messages): Seq[SummaryListRow] = {
    val nameRow: Option[SummaryListRow]         = QROPSNameSummary.row(mode, userAnswers, showChangeLinks)
    val referenceRow: Option[SummaryListRow]    = QROPSReferenceSummary.row(mode, userAnswers, showChangeLinks)
    val addressRow: Option[SummaryListRow]      = QROPSAddressSummary.row(mode, userAnswers, showChangeLinks)
    val countryRow: Option[SummaryListRow]      = QROPSCountrySummary.row(mode, userAnswers, showChangeLinks)
    val otherCountryRow: Option[SummaryListRow] = QROPSOtherCountrySummary.row(mode, userAnswers, showChangeLinks)

    Seq(
      nameRow,
      referenceRow,
      addressRow,
      countryRow,
      otherCountryRow
    ).flatten
  }
}

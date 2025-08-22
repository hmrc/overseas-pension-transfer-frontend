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

package viewmodels.checkAnswers.memberDetails

import models.{Mode, UserAnswers}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

case object MemberDetailsSummary {

  def rows(mode: Mode, userAnswers: UserAnswers)(implicit messages: Messages): Seq[SummaryListRow] = {
    val nameRow: Option[SummaryListRow]         = MemberNameSummary.row(mode, userAnswers)
    val ninoRow: Option[SummaryListRow]         = MemberNinoSummary.row(mode, userAnswers)
    val noNinoRow: Option[SummaryListRow]       = MemberDoesNotHaveNinoSummary.row(mode, userAnswers)
    val dobRow: Option[SummaryListRow]          = MemberDateOfBirthSummary.row(mode, userAnswers)
    val currentAddRow: Option[SummaryListRow]   = MembersCurrentAddressSummary.row(mode, userAnswers)
    val isResidentRow: Option[SummaryListRow]   = MemberIsResidentUKSummary.row(mode, userAnswers)
    val everResidentRow: Option[SummaryListRow] = MemberHasEverBeenResidentUKSummary.row(mode, userAnswers)
    val lastAddRow: Option[SummaryListRow]      = MembersLastUKAddressSummary.row(mode, userAnswers)
    val dolRow: Option[SummaryListRow]          = MemberDateOfLeavingUKSummary.row(mode, userAnswers)

    Seq(
      nameRow,
      ninoRow,
      noNinoRow,
      dobRow,
      currentAddRow,
      isResidentRow,
      everResidentRow,
      lastAddRow,
      dolRow
    ).flatten
  }
}

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

package viewmodels.checkAnswers

import models.UserAnswers
import pages.memberDetails.MemberNamePage
import play.api.i18n.{Lang, Messages}
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.twirl.api.HtmlFormat
import queries.DateSubmittedQuery
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.DateTimeFormats.dateTimeFormat
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

case object TransferSubmittedSummary {

  def rows(userAnswers: UserAnswers)(implicit messages: Messages): Seq[SummaryListRow] = {
    implicit val lang: Lang = messages.lang

    val memberNameRow: Option[SummaryListRow] = userAnswers.get(MemberNamePage).map {
      answer =>
        val value = s"${HtmlFormat.escape(answer.firstName)} ${HtmlFormat.escape(answer.lastName)}"

        SummaryListRowViewModel(
          key   = "transferSubmitted.memberName.key",
          value = ValueViewModel(HtmlContent(value))
        )
    }

    val timeSubmittedRow = userAnswers.get(DateSubmittedQuery).map {
      answer =>
        SummaryListRowViewModel(
          key   = "transferSubmitted.dateSubmitted.key",
          value = ValueViewModel(HtmlContent(answer.format(dateTimeFormat())))
        )
    }

    Seq(
      memberNameRow,
      timeSubmittedRow
    ).flatten
  }
}

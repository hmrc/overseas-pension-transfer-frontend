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

import viewmodels.implicits._
import play.api.mvc.AnyContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import play.api.i18n.Messages
import models.requests.SchemeRequest
import viewmodels.govuk.summarylist._

case object TransferSubmittedSummary {

  def rows(memberName: String, dateTransferSubmitted: String)(implicit
    request: SchemeRequest[AnyContent],
    messages: Messages
  ): SummaryList = {
    val memberNameRow: SummaryListRow =
      SummaryListRowViewModel(
        key = "transferSubmitted.memberName.key",
        value = ValueViewModel(HtmlContent(memberName))
      )

    val schemeName: String =
      request.schemeDetails.schemeName

    val schemeNameRow: SummaryListRow =
      SummaryListRowViewModel(
        key = "transferSubmitted.pensionScheme.key",
        value = ValueViewModel(HtmlContent(schemeName))
      )

    val timeSubmittedRow: SummaryListRow =
      SummaryListRowViewModel(
        key = "transferSubmitted.dateSubmitted.key",
        value = ValueViewModel(HtmlContent(dateTransferSubmitted))
      )

    SummaryList(
      Seq(
        memberNameRow,
        schemeNameRow,
        timeSubmittedRow
      )
    )
  }
}

/*
 * Copyright 2026 HM Revenue & Customs
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

package views

import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import views.html.checkYourAnswers.CheckYourAnswersView
import views.utils.ViewBaseSpec

class CheckYourAnswersViewSpec extends ViewBaseSpec {

  private val view = applicationBuilder().injector().instanceOf[CheckYourAnswersView]

  private val memberDetails        = SummaryList()
  private val transferDetails      = SummaryList()
  private val qropsDetails         = SummaryList()
  private val schemeManagerDetails = SummaryList()

  "CheckYourAnswersView" - {

    "show correct title" in {
      doc(view(memberDetails, transferDetails, qropsDetails, schemeManagerDetails).body)
        .getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("checkYourAnswers.title")} - ${messages("service.name")} - GOV.UK"
    }

    behave like pageWithH1(
      view(memberDetails, transferDetails, qropsDetails, schemeManagerDetails),
      "checkYourAnswers.heading"
    )

    behave like pageWithHeadings(
      view(memberDetails, transferDetails, qropsDetails, schemeManagerDetails),
      "h2",
      "common.memberDetails.heading",
      "common.transferDetails.heading",
      "common.qropsDetails.heading",
      "common.qropsSchemeManagerDetails.heading"
    )

    behave like pageWithSubmitButton(
      view(memberDetails, transferDetails, qropsDetails, schemeManagerDetails),
      "site.continue"
    )

    "display links to discard and return to task list" in {
      val links = doc(view(memberDetails, transferDetails, qropsDetails, schemeManagerDetails).body)
        .getElementsByTag("a")

      val discardLink = links.select("#discardReportLink").first()
      discardLink.text() mustBe messages("footer.link.text.discard.report")

      val taskListLink = links.select("#returnTaskListLink").first()
      taskListLink.text() mustBe messages("footer.link.text.tasklist")
    }
  }
}

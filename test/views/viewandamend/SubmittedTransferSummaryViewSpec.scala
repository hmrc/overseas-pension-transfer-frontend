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

package views.viewandamend

import models.requests.SchemeRequest
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html
import views.html.viewandamend.SubmittedTransferSummaryView
import views.utils.ViewBaseSpec

class SubmittedTransferSummaryViewSpec extends ViewBaseSpec {

  private val view = applicationBuilder().injector().instanceOf[SubmittedTransferSummaryView]

  private val memberName = "John Doe"
  private val qtNumber   = "QT123456"
  private val tableRows  = Html("<tr><td>Test</td></tr>")

  implicit val schemeRequest: SchemeRequest[AnyContentAsEmpty.type] = fakeSchemeRequest(FakeRequest())

  "SubmittedTransferSummaryView" - {

    "show correct title with member name" in {
      doc(view(memberName, qtNumber, tableRows).body)
        .getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("submittedTransferSummary.title", memberName)} - ${messages("service.name")} - GOV.UK"
    }

    "display correct heading with member name" in {
      val heading = doc(view(memberName, qtNumber, tableRows).body)
        .getElementsByTag("h1").first()
      heading.text() mustBe messages("submittedTransferSummary.heading", memberName)
    }

    "display QT number as caption" in {
      val caption = doc(view(memberName, qtNumber, tableRows).body)
        .getElementsByClass("govuk-caption-l").first()
      caption.text() mustBe qtNumber
    }

    "display table structure" in {
      val table = doc(view(memberName, qtNumber, tableRows).body)
        .getElementsByClass("govuk-table").first()

      table must not be null

      val headers = table.getElementsByClass("govuk-table__header")
      headers.size() mustBe 3
    }

    "display table with provided rows" in {
      val tableBody = doc(view(memberName, qtNumber, tableRows).body)
        .getElementsByClass("govuk-table__body").first()

      tableBody.html() must include("Test")
    }
  }
}

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

import controllers.routes
import models.requests.SchemeRequest
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import views.html.PrintSubmittedTransferView
import views.utils.ViewBaseSpec

class PrintSubmittedTransferViewSpec extends ViewBaseSpec {

  private val view = applicationBuilder().injector().instanceOf[PrintSubmittedTransferView]

  implicit val schemeRequest: SchemeRequest[_] = SchemeRequest(
    request           = FakeRequest(),
    authenticatedUser = psaUser,
    schemeDetails     = schemeDetails
  )

  private val testQtNumberValue    = "QT123456"
  private val mpsLink              = "/mps-link"
  private val overviewDetails      = SummaryList()
  private val memberDetails        = SummaryList()
  private val transferDetails      = SummaryList()
  private val qropsDetails         = SummaryList()
  private val schemeManagerDetails = SummaryList()
  private val managerEmail         = "someone@example.com"
  "PrintSubmittedTransferView" - {

    "show correct title" in {
      doc(
        view(testQtNumberValue, overviewDetails, memberDetails, transferDetails, qropsDetails, schemeManagerDetails, managerEmail, mpsLink).body
      ).getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("transferSubmitted.title")} - ${messages("service.name")} - GOV.UK"
    }

    behave like pageWithConfirmationPanel(
      view(testQtNumberValue, overviewDetails, memberDetails, transferDetails, qropsDetails, schemeManagerDetails, managerEmail, mpsLink),
      "transferSubmitted.heading",
      "transferSubmitted.referenceNumber.text",
      testQtNumberValue
    )

    "display correct links" in {
      val links = doc(
        view(testQtNumberValue, overviewDetails, memberDetails, transferDetails, qropsDetails, schemeManagerDetails, managerEmail, mpsLink).body
      ).getElementById("main-content").getElementsByTag("a")

      links.get(0).text() mustBe messages("transferSubmitted.dashboardLink.text")
      links.get(0).attr("href") mustBe routes.DashboardController.onPageLoad().url

      links.get(1).text() mustBe messages("transferSubmitted.pensionSchemeLink.text", schemeDetails.schemeName)
      links.get(1).attr("href") mustBe mpsLink
    }

    "display transfer details for print" in {
      val printDocScreen = doc(
        view(testQtNumberValue, overviewDetails, memberDetails, transferDetails, qropsDetails, schemeManagerDetails, managerEmail, mpsLink).body
      ).getElementById("main-content")
      val headings       = printDocScreen.getElementsByTag("h2")

      headings.get(0).text() mustBe messages("transferSubmitted.memberDetails")
      headings.get(1).text() mustBe messages("transferSubmitted.transferDetails")
      headings.get(2).text() mustBe messages("transferSubmitted.qropsDetails")
      headings.get(3).text() mustBe messages("transferSubmitted.qropsManagerDetails")

      printDocScreen.getElementById("print-button").text() mustBe messages("site.print")
    }
  }
}

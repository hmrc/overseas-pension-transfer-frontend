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

package views.qropsDetails

import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import views.html.qropsDetails.QROPSDetailsCYAView
import views.utils.ViewBaseSpec

class QROPSDetailsCYAViewSpec extends ViewBaseSpec {

  private val view = applicationBuilder().injector().instanceOf[QROPSDetailsCYAView]

  private val summaryList = SummaryList()

  "QROPSDetailsCYAView" - {

    "show correct title" in {
      doc(view(summaryList).body).getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("checkYourAnswers.QROPSDetails.title")} - ${messages("service.name")} - GOV.UK"
    }

    behave like pageWithH1(view(summaryList), "checkYourAnswers.QROPSDetails.heading")

    behave like pageWithSubmitButton(view(summaryList), "site.continue")

    "display summary list" in {
      val summaryLists = doc(view(summaryList).body).getElementsByClass("govuk-summary-list")
      summaryLists.size() mustBe 1
    }
  }
}

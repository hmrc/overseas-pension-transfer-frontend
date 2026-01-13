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

package views.transferDetails.assetsMiniJourneys.unquotedShares

import views.html.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesStartView
import views.utils.ViewBaseSpec

class UnquotedSharesStartViewSpec extends ViewBaseSpec {

  private val view = applicationBuilder().injector().instanceOf[UnquotedSharesStartView]

  private val nextPage = "/next-page-url"

  "UnquotedSharesStartView" - {

    "show correct title" in {
      doc(view(nextPage).body)
        .getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("unquotedShareStart.title")} - ${messages("service.name")} - GOV.UK"
    }

    behave like pageWithH1(view(nextPage), "unquotedShareStart.heading")

    "display informational text" in {
      val paragraphs = doc(view(nextPage).body)
        .getElementsByClass("govuk-body")

      paragraphs.size() must be > 0
      paragraphs.first().text() mustBe messages("unquotedShareStart.paragraph")
    }

    "display continue button as link" in {
      val button = doc(view(nextPage).body)
        .select("a.govuk-button")

      button.size() mustBe 1
      button.first().attr("href") mustBe nextPage
      button.first().text() must include(messages("site.continue"))
    }
  }
}

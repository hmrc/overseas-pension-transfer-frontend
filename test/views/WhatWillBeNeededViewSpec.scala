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

import views.html.WhatWillBeNeededView
import views.utils.ViewBaseSpec

class WhatWillBeNeededViewSpec extends ViewBaseSpec {

  private val view = applicationBuilder().injector().instanceOf[WhatWillBeNeededView]

  "WhatWillBeNeededView" - {

    "show correct title" in {
      doc(view().body).getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("requiredInfo.title")} - ${messages("service.name")} - GOV.UK"
    }

    behave like pageWithH1(view(), "requiredInfo.heading")

    behave like pageWithHeadings(
      view(),
      "h2",
      "requiredInfo.memberDetails.heading",
      "requiredInfo.transferDetails.heading",
      "requiredInfo.qropsDetails.heading",
      "requiredInfo.schemeManager.heading"
    )

    behave like pageWithSubmitButton(view(), "site.continue")

    "display member details bullet list" in {
      val bulletLists       = doc(view().body).getElementsByClass("govuk-list--bullet")
      val memberDetailsList = bulletLists.get(0).getElementsByTag("li")

      memberDetailsList.size() mustBe 5
      memberDetailsList.get(0).text() mustBe messages("requiredInfo.memberDetails.bullet1")
      memberDetailsList.get(4).text() mustBe messages("requiredInfo.memberDetails.bullet5")
    }

    "display transfer details bullet list" in {
      val bulletLists         = doc(view().body).getElementsByClass("govuk-list--bullet")
      val transferDetailsList = bulletLists.get(1).getElementsByTag("li")

      transferDetailsList.size() mustBe 7
      transferDetailsList.get(0).text() mustBe messages("requiredInfo.transferDetails.bullet1")
      transferDetailsList.get(6).text() mustBe messages("requiredInfo.transferDetails.bullet7")
    }

    "display qrops details bullet list" in {
      val bulletLists      = doc(view().body).getElementsByClass("govuk-list--bullet")
      val qropsDetailsList = bulletLists.get(2).getElementsByTag("li")

      qropsDetailsList.size() mustBe 4
      qropsDetailsList.get(0).text() mustBe messages("requiredInfo.qropsDetails.bullet1")
      qropsDetailsList.get(3).text() mustBe messages("requiredInfo.qropsDetails.bullet4")
    }

    "display scheme manager bullet list" in {
      val bulletLists       = doc(view().body).getElementsByClass("govuk-list--bullet")
      val schemeManagerList = bulletLists.get(3).getElementsByTag("li")

      schemeManagerList.size() mustBe 2
      schemeManagerList.get(0).text() mustBe messages("requiredInfo.schemeManager.bullet1")
      schemeManagerList.get(1).text() mustBe messages("requiredInfo.schemeManager.bullet2")
    }
  }
}

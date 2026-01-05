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

import forms.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesAmendContinueFormProvider
import models.NormalMode
import play.api.data.FormError
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.addtoalist.ListItem
import views.html.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesAmendContinueView
import views.utils.ViewBaseSpec

class UnquotedSharesAmendContinueViewSpec extends ViewBaseSpec {

  private val view         = applicationBuilder().injector().instanceOf[UnquotedSharesAmendContinueView]
  private val formProvider = applicationBuilder().injector().instanceOf[UnquotedSharesAmendContinueFormProvider]

  private val testShares = Seq(
    ListItem("Share 1", "change-url-1", "remove-url-1"),
    ListItem("Share 2", "change-url-2", "remove-url-2")
  )

  "UnquotedSharesAmendContinueView" - {

    "show correct title with no items" in {
      doc(view(formProvider(), Seq.empty, NormalMode).body)
        .getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("unquotedSharesAmendContinue.text.title.noItems")} - ${messages("service.name")} - GOV.UK"
    }

    "show correct title with one item" in {
      val oneItem = Seq(ListItem("Share 1", "change-url-1", "remove-url-1"))
      doc(view(formProvider(), oneItem, NormalMode).body)
        .getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("unquotedSharesAmendContinue.text.title.oneItem")} - ${messages("service.name")} - GOV.UK"
    }

    "show correct title with multiple items" in {
      doc(view(formProvider(), testShares, NormalMode).body)
        .getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("unquotedSharesAmendContinue.text.title.multipleItems", testShares.length)} - ${messages("service.name")} - GOV.UK"
    }

    "display add to a list component" in {
      val addToAList = doc(view(formProvider(), testShares, NormalMode).body)
        .getElementsByClass("hmrc-add-to-a-list")

      addToAList.size() must be >= 0
    }

    behave like pageWithErrors(
      view(formProvider().withError(FormError("add-another", "unquotedSharesAmendContinue.error.required")), testShares, NormalMode),
      "add-another",
      "unquotedSharesAmendContinue.error.required"
    )
  }
}

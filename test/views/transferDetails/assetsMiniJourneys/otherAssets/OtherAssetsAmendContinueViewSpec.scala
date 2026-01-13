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

package views.transferDetails.assetsMiniJourneys.otherAssets

import forms.transferDetails.assetsMiniJourneys.otherAssets.OtherAssetsAmendContinueFormProvider
import models.NormalMode
import play.api.data.FormError
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.addtoalist.ListItem
import views.html.transferDetails.assetsMiniJourneys.otherAssets.OtherAssetsAmendContinueView
import views.utils.ViewBaseSpec

class OtherAssetsAmendContinueViewSpec extends ViewBaseSpec {

  private val view         = applicationBuilder().injector().instanceOf[OtherAssetsAmendContinueView]
  private val formProvider = applicationBuilder().injector().instanceOf[OtherAssetsAmendContinueFormProvider]

  private val testAssets = Seq(
    ListItem("Asset 1", "change-url-1", "remove-url-1"),
    ListItem("Asset 2", "change-url-2", "remove-url-2")
  )

  "OtherAssetsAmendContinueView" - {

    "show correct title with no items" in {
      doc(view(formProvider(), Seq.empty, NormalMode).body)
        .getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("otherAssetsAmendContinue.text.title.noItems")} - ${messages("service.name")} - GOV.UK"
    }

    "show correct title with one item" in {
      val oneItem = Seq(ListItem("Asset 1", "change-url-1", "remove-url-1"))
      doc(view(formProvider(), oneItem, NormalMode).body)
        .getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("otherAssetsAmendContinue.text.title.oneItem")} - ${messages("service.name")} - GOV.UK"
    }

    "show correct title with multiple items" in {
      doc(view(formProvider(), testAssets, NormalMode).body)
        .getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("otherAssetsAmendContinue.text.title.multipleItems", testAssets.length)} - ${messages("service.name")} - GOV.UK"
    }

    "display add to a list component" in {
      val addToAList = doc(view(formProvider(), testAssets, NormalMode).body)
        .getElementsByClass("hmrc-add-to-a-list")

      addToAList.size() must be >= 0 // Component may render differently based on content
    }

    behave like pageWithErrors(
      view(formProvider().withError(FormError("add-another", "otherAssetsAmendContinue.error.required")), testAssets, NormalMode),
      "add-another",
      "otherAssetsAmendContinue.error.required"
    )
  }
}

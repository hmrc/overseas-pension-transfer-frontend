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

import forms.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesConfirmRemovalFormProvider
import play.api.data.FormError
import views.html.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesConfirmRemovalView
import views.utils.ViewBaseSpec

class UnquotedSharesConfirmRemovalViewSpec extends ViewBaseSpec {

  private val view         = applicationBuilder().injector().instanceOf[UnquotedSharesConfirmRemovalView]
  private val formProvider = applicationBuilder().injector().instanceOf[UnquotedSharesConfirmRemovalFormProvider]

  private val testIndex = 0

  "UnquotedSharesConfirmRemovalView" - {

    "show correct title" in {
      doc(view(formProvider(), testIndex).body)
        .getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("unquotedSharesConfirmRemoval.title")} - ${messages("service.name")} - GOV.UK"
    }

    behave like pageWithH1(view(formProvider(), testIndex), "unquotedSharesConfirmRemoval.heading")

    "display yes/no radio buttons" in {
      val radios = doc(view(formProvider(), testIndex).body)
        .getElementsByClass("govuk-radios__item")

      radios.size() mustBe 2
    }

    behave like pageWithSubmitButton(view(formProvider(), testIndex), "site.saveAndContinue")

    behave like pageWithErrors(
      view(formProvider().withError(FormError("value", "unquotedSharesConfirmRemoval.error.required")), testIndex),
      "value",
      "unquotedSharesConfirmRemoval.error.required"
    )
  }
}

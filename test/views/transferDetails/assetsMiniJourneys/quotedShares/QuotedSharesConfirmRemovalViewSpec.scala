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

package views.transferDetails.assetsMiniJourneys.quotedShares

import forms.transferDetails.assetsMiniJourneys.quotedShares.QuotedSharesConfirmRemovalFormProvider
import play.api.data.FormError
import views.html.transferDetails.assetsMiniJourneys.quotedShares.QuotedSharesConfirmRemovalView
import views.utils.ViewBaseSpec

class QuotedSharesConfirmRemovalViewSpec extends ViewBaseSpec {

  private val view         = applicationBuilder().injector().instanceOf[QuotedSharesConfirmRemovalView]
  private val formProvider = applicationBuilder().injector().instanceOf[QuotedSharesConfirmRemovalFormProvider]

  private val testIndex = 0

  "QuotedSharesConfirmRemovalView" - {

    "show correct title" in {
      doc(view(formProvider(), testIndex).body)
        .getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("quotedSharesConfirmRemoval.title")} - ${messages("service.name")} - GOV.UK"
    }

    behave like pageWithH1(view(formProvider(), testIndex), "quotedSharesConfirmRemoval.heading")

    "display yes/no radio buttons" in {
      val radios = doc(view(formProvider(), testIndex).body)
        .getElementsByClass("govuk-radios__item")

      radios.size() mustBe 2
    }

    behave like pageWithSubmitButton(view(formProvider(), testIndex), "site.saveAndContinue")

    behave like pageWithErrors(
      view(formProvider().withError(FormError("value", "quotedSharesConfirmRemoval.error.required")), testIndex),
      "value",
      "quotedSharesConfirmRemoval.error.required"
    )
  }
}

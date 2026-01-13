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

import forms.transferDetails.assetsMiniJourneys.quotedShares.QuotedSharesValueFormProvider
import models.NormalMode
import play.api.data.FormError
import views.html.transferDetails.assetsMiniJourneys.quotedShares.QuotedSharesValueView
import views.utils.ViewBaseSpec

class QuotedSharesValueViewSpec extends ViewBaseSpec {

  private val view         = applicationBuilder().injector().instanceOf[QuotedSharesValueView]
  private val formProvider = applicationBuilder().injector().instanceOf[QuotedSharesValueFormProvider]

  private val testIndex = 0

  "QuotedSharesValueView" - {

    "show correct title" in {
      doc(view(formProvider(), NormalMode, testIndex).body)
        .getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("quotedSharesValue.title")} - ${messages("service.name")} - GOV.UK"
    }

    behave like pageWithH1(view(formProvider(), NormalMode, testIndex), "quotedSharesValue.heading")

    behave like pageWithInputField(view(formProvider(), NormalMode, testIndex), "value", "quotedSharesValue.heading")

    behave like pageWithSubmitButton(view(formProvider(), NormalMode, testIndex), "site.saveAndContinue")

    behave like pageWithErrors(
      view(formProvider().withError(FormError("value", "quotedSharesValue.error.required")), NormalMode, testIndex),
      "value",
      "quotedSharesValue.error.required"
    )
  }
}

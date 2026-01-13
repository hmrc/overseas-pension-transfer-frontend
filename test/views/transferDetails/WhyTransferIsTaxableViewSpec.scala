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

package views.transferDetails

import forms.transferDetails.WhyTransferIsTaxableFormProvider
import models.NormalMode
import play.api.data.FormError
import views.html.transferDetails.WhyTransferIsTaxableView
import views.utils.ViewBaseSpec

class WhyTransferIsTaxableViewSpec extends ViewBaseSpec {

  private val view         = applicationBuilder().injector().instanceOf[WhyTransferIsTaxableView]
  private val formProvider = applicationBuilder().injector().instanceOf[WhyTransferIsTaxableFormProvider]

  "WhyTransferIsTaxableView" - {

    "show correct title" in {
      doc(view(formProvider(), NormalMode).body).getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("whyTransferIsTaxable.title")} - ${messages("service.name")} - GOV.UK"
    }

    behave like pageWithH1(view(formProvider(), NormalMode), "whyTransferIsTaxable.heading")

    "display radio buttons" in {
      val radios = doc(view(formProvider(), NormalMode).body).getElementsByClass("govuk-radios__item")
      radios.size() must be > 0
    }

    behave like pageWithSubmitButton(view(formProvider(), NormalMode), "site.saveAndContinue")

    behave like pageWithErrors(
      view(formProvider().withError(FormError("value", "whyTransferIsTaxable.error.required")), NormalMode),
      "value",
      "whyTransferIsTaxable.error.required"
    )
  }
}

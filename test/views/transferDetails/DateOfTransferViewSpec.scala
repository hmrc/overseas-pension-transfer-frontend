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

import forms.transferDetails.DateOfTransferFormProvider
import models.NormalMode
import play.api.data.FormError
import views.html.transferDetails.DateOfTransferView
import views.utils.ViewBaseSpec

class DateOfTransferViewSpec extends ViewBaseSpec {

  private val view         = applicationBuilder().injector().instanceOf[DateOfTransferView]
  private val formProvider = applicationBuilder().injector().instanceOf[DateOfTransferFormProvider]

  "DateOfTransferView" - {

    "show correct title" in {
      doc(view(formProvider(), NormalMode).body).getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("dateOfTransfer.title")} - ${messages("service.name")} - GOV.UK"
    }

    behave like pageWithH1(view(formProvider(), NormalMode), "dateOfTransfer.heading")

    "display date input with hint" in {
      val dateInputs = doc(view(formProvider(), NormalMode).body).getElementsByClass("govuk-date-input")
      dateInputs.size() mustBe 1
    }

    behave like pageWithSubmitButton(view(formProvider(), NormalMode), "site.saveAndContinue")

    behave like pageWithErrors(
      view(formProvider().withError(FormError("value", "dateOfTransfer.error.required.all")), NormalMode),
      "value",
      "dateOfTransfer.error.required.all"
    )
  }
}

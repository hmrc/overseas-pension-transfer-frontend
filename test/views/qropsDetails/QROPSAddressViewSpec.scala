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

import forms.qropsDetails.QROPSAddressFormProvider
import models.NormalMode
import play.api.data.FormError
import viewmodels.CountrySelectViewModel
import views.html.qropsDetails.QROPSAddressView
import views.utils.ViewBaseSpec

class QROPSAddressViewSpec extends ViewBaseSpec {

  private val view                   = applicationBuilder().injector().instanceOf[QROPSAddressView]
  private val formProvider           = applicationBuilder().injector().instanceOf[QROPSAddressFormProvider]
  private val countrySelectViewModel = CountrySelectViewModel(Seq.empty)

  "QROPSAddressView" - {

    "show correct title" in {
      doc(view(formProvider(), countrySelectViewModel, NormalMode).body)
        .getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("qropsAddress.title")} - ${messages("service.name")} - GOV.UK"
    }

    behave like pageWithH1(view(formProvider(), countrySelectViewModel, NormalMode), "qropsAddress.heading")

    behave like pageWithMultipleInputFields(
      view(formProvider(), countrySelectViewModel, NormalMode),
      ("addressLine1", "common.addressInput.addressLine1"),
      ("addressLine2", "common.addressInput.addressLine2"),
      ("addressLine3", "common.addressInput.addressLine3"),
      ("addressLine4", "common.addressInput.addressLine4"),
      ("addressLine5", "common.addressInput.addressLine5")
    )

    behave like pageWithSubmitButton(
      view(formProvider(), countrySelectViewModel, NormalMode),
      "site.saveAndContinue"
    )

    behave like pageWithErrors(
      view(
        formProvider().withError(FormError("addressLine1", "qropsAddress.error.addressLine1.required")),
        countrySelectViewModel,
        NormalMode
      ),
      "addressLine1",
      "qropsAddress.error.addressLine1.required"
    )
  }
}

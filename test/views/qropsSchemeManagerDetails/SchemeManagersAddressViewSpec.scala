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

package views.qropsSchemeManagerDetails

import config.FrontendAppConfig
import forms.qropsSchemeManagerDetails.SchemeManagersAddressFormProvider
import models.NormalMode
import play.api.data.FormError
import viewmodels.CountrySelectViewModel
import views.html.qropsSchemeManagerDetails.SchemeManagersAddressView
import views.utils.ViewBaseSpec

class SchemeManagersAddressViewSpec extends ViewBaseSpec {

  private val application = applicationBuilder().configure(
    "features.accessibility-address-changes" -> true
  ).build()

  private val view                                  = application.injector.instanceOf[SchemeManagersAddressView]
  private val formProvider                          = application.injector.instanceOf[SchemeManagersAddressFormProvider]
  private val countrySelectViewModel                = CountrySelectViewModel(Seq.empty)
  implicit private val appConfig: FrontendAppConfig = application.injector.instanceOf[config.FrontendAppConfig]

  "SchemeManagersAddressView" - {

    "show correct title" in {
      doc(view(formProvider(), countrySelectViewModel, NormalMode).body)
        .getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("schemeManagersAddress.title")} - ${messages("service.name")} - GOV.UK"
    }

    behave like pageWithH1(view(formProvider(), countrySelectViewModel, NormalMode), "schemeManagersAddress.heading")

    behave like pageWithMultipleInputFields(
      view(formProvider(), countrySelectViewModel, NormalMode),
      ("addressLine1", "common.addressInput.addressLine1"),
      ("addressLine2", "common.addressInput.addressLine2"),
      ("addressLine3", "common.addressInput.townOrCity"),
      ("addressLine4", "common.addressInput.postcode"),
      ("addressLine5", "common.addressInput.poBox")
    )

    behave like pageWithSubmitButton(
      view(formProvider(), countrySelectViewModel, NormalMode),
      "site.saveAndContinue"
    )

    behave like pageWithErrors(
      view(
        formProvider().withError(FormError("addressLine1", "schemeManagersAddress.error.addressLine1.required")),
        countrySelectViewModel,
        NormalMode
      ),
      "addressLine1",
      "schemeManagersAddress.error.addressLine1.required"
    )
  }
}

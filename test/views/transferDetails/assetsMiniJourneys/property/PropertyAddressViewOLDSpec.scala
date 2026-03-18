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

package views.transferDetails.assetsMiniJourneys.property

import config.FrontendAppConfig
import forms.transferDetails.assetsMiniJourneys.property.PropertyAddressFormProvider
import models.NormalMode
import play.api.Application
import play.api.data.FormError
import play.api.inject.guice.GuiceApplicationBuilder
import viewmodels.CountrySelectViewModel
import views.html.transferDetails.assetsMiniJourneys.property.PropertyAddressView
import views.utils.ViewBaseSpec

class PropertyAddressViewOLDSpec extends ViewBaseSpec {

  val application: Application = GuiceApplicationBuilder()
    .configure("features.accessibility-address-changes" -> false)
    .build()

  private val view                                  = application.injector.instanceOf[PropertyAddressView]
  private val formProvider                          = application.injector.instanceOf[PropertyAddressFormProvider]
  private val countrySelectViewModel                = CountrySelectViewModel(Seq.empty)
  private val testIndex                             = 0
  implicit private val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

  "PropertyAddressView" - {

    "show correct title" in {
      doc(view(formProvider(false), countrySelectViewModel, NormalMode, testIndex).body)
        .getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("propertyAddress.title")} - ${messages("service.name")} - GOV.UK"
    }

    behave like pageWithH1(view(formProvider(false), countrySelectViewModel, NormalMode, testIndex), "propertyAddress.heading")

    behave like pageWithMultipleInputFields(
      view(formProvider(false), countrySelectViewModel, NormalMode, testIndex),
      ("addressLine1", "common.addressInput.addressLine1"),
      ("addressLine2", "common.addressInput.addressLine2"),
      ("addressLine3", "common.addressInput.addressLine3"),
      ("addressLine4", "common.addressInput.addressLine4"),
      ("postcode", "common.addressInput.postcode")
    )

    behave like pageWithSubmitButton(
      view(formProvider(false), countrySelectViewModel, NormalMode, testIndex),
      "site.saveAndContinue"
    )

    behave like pageWithErrors(
      view(
        formProvider(false).withError(FormError("addressLine1", "propertyAddress.error.addressLine1.required")),
        countrySelectViewModel,
        NormalMode,
        testIndex
      ),
      "addressLine1",
      "propertyAddress.error.addressLine1.required"
    )
  }
}

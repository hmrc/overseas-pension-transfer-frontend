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

package views.memberDetails

import forms.memberDetails.MembersCurrentAddressFormProvider
import models.NormalMode
import play.api.data.FormError
import viewmodels.CountrySelectViewModel
import views.html.memberDetails.MembersCurrentAddressView
import views.utils.ViewBaseSpec

class MembersCurrentAddressViewSpec extends ViewBaseSpec {

  private val view                   = applicationBuilder().injector().instanceOf[MembersCurrentAddressView]
  private val formProvider           = applicationBuilder().injector().instanceOf[MembersCurrentAddressFormProvider]
  private val countrySelectViewModel = CountrySelectViewModel(Seq.empty)
  "MembersCurrentAddressView" - {

    "show correct title" in {
      doc(view(formProvider(), countrySelectViewModel, NormalMode).body)
        .getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("membersCurrentAddress.title")} - ${messages("service.name")} - GOV.UK"
    }

    "display correct heading with member name" in {
      val heading = doc(view(formProvider(), countrySelectViewModel, NormalMode).body).getElementsByTag("h1").first()
      heading.text() mustBe messages("membersCurrentAddress.heading", testMemberName.fullName)
    }

    behave like pageWithMultipleInputFields(
      view(formProvider(), countrySelectViewModel, NormalMode),
      ("addressLine1", "common.addressInput.addressLine1"),
      ("addressLine2", "common.addressInput.addressLine2"),
      ("addressLine3", "common.addressInput.addressLine3"),
      ("addressLine4", "common.addressInput.addressLine4"),
      ("postcode", "common.addressInput.postcode")
    )

    behave like pageWithSubmitButton(
      view(formProvider(), countrySelectViewModel, NormalMode),
      "site.saveAndContinue"
    )

    behave like pageWithErrors(
      view(
        formProvider().withError(FormError("addressLine1", "membersCurrentAddress.error.addressLine1.required")),
        countrySelectViewModel,
        NormalMode
      ),
      "addressLine1",
      "membersCurrentAddress.error.addressLine1.required"
    )
  }
}

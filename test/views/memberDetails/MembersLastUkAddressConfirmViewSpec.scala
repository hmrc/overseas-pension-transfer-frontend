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

import forms.memberDetails.MembersLastUKAddressFormProvider
import models.NormalMode
import viewmodels.AddressViewModel
import views.html.memberDetails.MembersLastUkAddressConfirmView
import views.utils.ViewBaseSpec

class MembersLastUkAddressConfirmViewSpec extends ViewBaseSpec {

  private val view         = applicationBuilder().injector().instanceOf[MembersLastUkAddressConfirmView]
  private val formProvider = applicationBuilder().injector().instanceOf[MembersLastUKAddressFormProvider]

  private val testAddress = AddressViewModel(
    addressLine1 = "1 Test Street",
    addressLine2 = "Test Town",
    addressLine3 = None,
    addressLine4 = None,
    addressLine5 = None,
    country      = "United Kingdom",
    ukPostCode   = Some("AB1 2CD"),
    poBox        = None
  )

  "MembersLastUkAddressConfirmView" - {

    "show correct title" in {
      doc(view(formProvider(), NormalMode, testAddress).body)
        .getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("memberConfirmLastUkAddress.title")} - ${messages("service.name")} - GOV.UK"
    }

    "display correct heading with member name" in {
      val heading = doc(view(formProvider(), NormalMode, testAddress).body).getElementsByTag("h1").first()
      heading.text() mustBe messages("memberConfirmLastUkAddress.heading", testMemberName.fullName)
    }

    "display address" in {
      val fullText = doc(view(formProvider(), NormalMode, testAddress).body).text()

      fullText must include("1 Test Street")
      fullText must include("Test Town")
      fullText must include("AB1 2CD")
    }

    behave like pageWithSubmitButton(
      view(formProvider(), NormalMode, testAddress),
      "site.saveAndContinue"
    )
  }
}

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

import forms.memberDetails.MembersLastUkAddressLookupFormProvider
import models.NormalMode
import play.api.data.FormError
import views.html.memberDetails.MembersLastUkAddressLookupView
import views.utils.ViewBaseSpec

class MembersLastUkAddressLookupViewSpec extends ViewBaseSpec {

  private val view         = applicationBuilder().injector().instanceOf[MembersLastUkAddressLookupView]
  private val formProvider = applicationBuilder().injector().instanceOf[MembersLastUkAddressLookupFormProvider]

  "MembersLastUkAddressLookupView" - {

    "show correct title" in {
      doc(view(formProvider(), NormalMode).body).getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("membersLastUkAddressLookup.title")} - ${messages("service.name")} - GOV.UK"
    }

    "display correct heading with member name" in {
      val heading = doc(view(formProvider(), NormalMode).body).getElementsByTag("h1").first()
      heading.text() mustBe messages("membersLastUkAddressLookup.heading", testMemberName.fullName)
    }

    behave like pageWithInputField(
      view(formProvider(), NormalMode),
      "value",
      "membersLastUkAddressLookup.label"
    )

    behave like pageWithSubmitButton(view(formProvider(), NormalMode), "site.continue")

    behave like pageWithErrors(
      view(formProvider().withError(FormError("value", "membersLastUkAddressLookup.error.required")), NormalMode),
      "value",
      "membersLastUkAddressLookup.error.required"
    )
  }
}

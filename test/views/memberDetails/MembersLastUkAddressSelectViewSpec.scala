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

import forms.memberDetails.MembersLastUkAddressSelectFormProvider
import models.NormalMode
import play.api.data.FormError
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import views.html.memberDetails.MembersLastUkAddressSelectView
import views.utils.ViewBaseSpec

class MembersLastUkAddressSelectViewSpec extends ViewBaseSpec {

  private val view         = applicationBuilder().injector().instanceOf[MembersLastUkAddressSelectView]
  private val formProvider = applicationBuilder().injector().instanceOf[MembersLastUkAddressSelectFormProvider]

  private val addressRadios = Seq(
    RadioItem(content = Text("1 Test Street, Test Town, AB1 2CD"), value = Some("0")),
    RadioItem(content = Text("2 Test Street, Test Town, AB1 2CD"), value = Some("1"))
  )

  private val validIds     = Seq("0", "1")
  private val testPostcode = "AB1 2CD"

  "MembersLastUkAddressSelectView" - {

    "show correct title" in {
      doc(view(formProvider(validIds), NormalMode, addressRadios, testPostcode).body)
        .getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("memberSelectLastUkAddress.title")} - ${messages("service.name")} - GOV.UK"
    }

    "display correct heading with member name" in {
      val heading = doc(view(formProvider(validIds), NormalMode, addressRadios, testPostcode).body)
        .getElementsByTag("legend").first()
      heading.text() mustBe messages("memberSelectLastUkAddress.heading", testMemberName.fullName)
    }

    "display radio buttons for address selection" in {
      val radios = doc(view(formProvider(validIds), NormalMode, addressRadios, testPostcode).body)
        .getElementsByClass("govuk-radios__item")

      radios.size() mustBe 2
    }

    behave like pageWithSubmitButton(
      view(formProvider(validIds), NormalMode, addressRadios, testPostcode),
      "site.continue"
    )

    behave like pageWithErrors(
      view(
        formProvider(validIds).withError(FormError("value", "memberSelectLastUkAddress.error.required")),
        NormalMode,
        addressRadios,
        testPostcode
      ),
      "value",
      "memberSelectLastUkAddress.error.required"
    )
  }
}

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

import forms.memberDetails.MemberIsResidentUKFormProvider
import models.NormalMode
import play.api.data.FormError
import views.html.memberDetails.MemberIsResidentUKView
import views.utils.ViewBaseSpec

class MemberIsResidentUKViewSpec extends ViewBaseSpec {

  private val view         = applicationBuilder().injector().instanceOf[MemberIsResidentUKView]
  private val formProvider = applicationBuilder().injector().instanceOf[MemberIsResidentUKFormProvider]

  "MemberIsResidentUKView" - {

    "show correct title" in {
      doc(view(formProvider(), NormalMode).body).getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("memberIsResidentUK.title")} - ${messages("service.name")} - GOV.UK"
    }

    "display correct heading with member name" in {
      val heading = doc(view(formProvider(), NormalMode).body).getElementsByTag("legend").first()
      heading.text() mustBe messages("memberIsResidentUK.heading", testMemberName.fullName)
    }

    behave like pageWithRadioButtons(view(formProvider(), NormalMode), "site.yes", "site.no")

    behave like pageWithSubmitButton(view(formProvider(), NormalMode), "site.saveAndContinue")

    behave like pageWithErrors(
      view(formProvider().withError(FormError("value", "memberIsResidentUK.error.required")), NormalMode),
      "value",
      "memberIsResidentUK.error.required"
    )
  }
}

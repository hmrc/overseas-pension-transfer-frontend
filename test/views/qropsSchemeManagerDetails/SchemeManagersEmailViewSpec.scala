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

import forms.qropsSchemeManagerDetails.SchemeManagersEmailFormProvider
import models.NormalMode
import play.api.data.FormError
import views.html.qropsSchemeManagerDetails.SchemeManagersEmailView
import views.utils.ViewBaseSpec

class SchemeManagersEmailViewSpec extends ViewBaseSpec {

  private val view         = applicationBuilder().injector().instanceOf[SchemeManagersEmailView]
  private val formProvider = applicationBuilder().injector().instanceOf[SchemeManagersEmailFormProvider]

  "SchemeManagersEmailView" - {

    "show correct title" in {
      doc(view(formProvider(), NormalMode).body).getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("schemeManagersEmail.title")} - ${messages("service.name")} - GOV.UK"
    }

    behave like pageWithH1(view(formProvider(), NormalMode), "schemeManagersEmail.heading")

    behave like pageWithInputField(view(formProvider(), NormalMode), "emailAddress", "schemeManagersEmail.heading")

    behave like pageWithSubmitButton(view(formProvider(), NormalMode), "site.saveAndContinue")

    behave like pageWithErrors(
      view(formProvider().withError(FormError("emailAddress", "schemeManagersEmail.error.required")), NormalMode),
      "emailAddress",
      "schemeManagersEmail.error.required"
    )
  }
}

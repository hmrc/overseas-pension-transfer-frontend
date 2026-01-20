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

package views

import forms.PspDeclarationFormProvider
import models.NormalMode
import play.api.data.FormError
import views.html.PspDeclarationView
import views.utils.ViewBaseSpec

class PspDeclarationViewSpec extends ViewBaseSpec {

  private val view         = applicationBuilder().injector().instanceOf[PspDeclarationView]
  private val formProvider = applicationBuilder().injector().instanceOf[PspDeclarationFormProvider]

  "PspDeclarationView" - {

    "show correct title" in {
      doc(view(formProvider(), NormalMode).body).getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("pspDeclaration.title")} - Report a transfer to a qualifying recognised overseas pension scheme - GOV.UK"
    }

    behave like pageWithH1(view(formProvider(), NormalMode), "pspDeclaration.heading")

    behave like pageWithText(view(formProvider(), NormalMode), "pspDeclaration.bySubmitting")

    behave like pageWithBulletList(
      view(formProvider(), NormalMode),
      "pspDeclaration.receivedAndChecked",
      "pspDeclaration.correctAndComplete"
    )

    behave like pageWithInputField(view(formProvider(), NormalMode), "value", "pspDeclaration.label")

    behave like pageWithSubmitButton(view(formProvider(), NormalMode), "site.agreeAndSubmit")

    behave like pageWithErrors(
      view(formProvider().withError(FormError("value", "pspDeclaration.error.required")), NormalMode),
      "value",
      "pspDeclaration.error.required"
    )
  }
}

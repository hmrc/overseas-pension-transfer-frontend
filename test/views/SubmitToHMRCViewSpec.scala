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

import forms.SubmitToHMRCFormProvider
import models.{AmendCheckMode, NormalMode}
import play.api.data.FormError
import views.html.SubmitToHMRCView
import views.utils.ViewBaseSpec

class SubmitToHMRCViewSpec extends ViewBaseSpec {

  private val view         = applicationBuilder().injector().instanceOf[SubmitToHMRCView]
  private val formProvider = applicationBuilder().injector().instanceOf[SubmitToHMRCFormProvider]

  "SubmitToHMRCView in NormalMode" - {

    "show correct title" in {
      doc(view(formProvider(), NormalMode).body).getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("submitToHMRC.title")} - Report a transfer to a qualifying recognised overseas pension scheme - GOV.UK"
    }

    behave like pageWithH1(view(formProvider(), NormalMode), "submitToHMRC.heading")

    behave like pageWithRadioButtons(view(formProvider(), NormalMode), "site.yes", "site.no")

    behave like pageWithErrors(
      view(formProvider().withError(FormError("value", "submitToHMRC.error.required")), NormalMode),
      "value",
      "submitToHMRC.error.required"
    )
  }

  "SubmitToHMRCView in AmendCheckMode" - {

    "show correct title" in {
      doc(view(formProvider(), AmendCheckMode).body).getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("submitToHMRC.amendTitle")} - Report a transfer to a qualifying recognised overseas pension scheme - GOV.UK"
    }

    behave like pageWithH1(view(formProvider(), AmendCheckMode), "submitToHMRC.amendHeading")

    behave like pageWithRadioButtons(view(formProvider(), AmendCheckMode), "site.yes", "site.no")
  }
}

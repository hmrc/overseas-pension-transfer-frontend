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

package views.viewandamend

import forms.viewandamend.ViewAmendSelectorFormProvider
import models.{PstrNumber, QtStatus}
import play.api.data.FormError
import views.html.viewandamend.ViewAmendSelectorView
import views.utils.ViewBaseSpec

class ViewAmendSelectorViewSpec extends ViewBaseSpec {

  private val view = applicationBuilder().injector().instanceOf[ViewAmendSelectorView]

  private val qtReference   = userAnswersTransferNumber
  private val pstrNumber    = PstrNumber("12345678")
  private val qtStatusValue = QtStatus.Submitted
  private val versionNumber = "001"

  "ViewAmendSelectorView" - {

    "show correct title" in {
      doc(view(qtReference, pstrNumber, qtStatusValue, versionNumber, ViewAmendSelectorFormProvider.form()).body)
        .getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("viewAmend.title")} - ${messages("service.name")} - GOV.UK"
    }

    "display correct heading" in {
      val heading = doc(view(qtReference, pstrNumber, qtStatusValue, versionNumber, ViewAmendSelectorFormProvider.form()).body)
        .getElementsByTag("h1").first()
      heading.text() mustBe messages("viewAmend.title")
    }

    "display view and amend radio options with correct values" in {
      val radios = doc(view(qtReference, pstrNumber, qtStatusValue, versionNumber, ViewAmendSelectorFormProvider.form()).body)
        .select("input[type=radio][name=viewOrAmend]")

      radios.size() mustBe 2
      radios.get(0).attr("value") mustBe "view"
      radios.get(1).attr("value") mustBe "amend"
    }

    "display correct labels for radio options" in {
      val labels = doc(view(qtReference, pstrNumber, qtStatusValue, versionNumber, ViewAmendSelectorFormProvider.form()).body)
        .getElementsByClass("govuk-radios__label")

      labels.get(0).text() mustBe messages("viewAmend.radio1")
      labels.get(1).text() mustBe messages("viewAmend.radio2")
    }

    behave like pageWithSubmitButton(
      view(qtReference, pstrNumber, qtStatusValue, versionNumber, ViewAmendSelectorFormProvider.form()),
      "site.continue"
    )

    behave like pageWithErrors(
      view(
        qtReference,
        pstrNumber,
        qtStatusValue,
        versionNumber,
        ViewAmendSelectorFormProvider.form().withError(FormError("viewOrAmend", "viewAmend.error.required"))
      ),
      "viewOrAmend",
      "viewAmend.error.required"
    )
  }
}

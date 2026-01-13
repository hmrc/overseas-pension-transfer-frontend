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

import forms.transferDetails.assetsMiniJourneys.property.PropertyDescriptionFormProvider
import models.NormalMode
import play.api.data.FormError
import views.html.transferDetails.assetsMiniJourneys.property.PropertyDescriptionView
import views.utils.ViewBaseSpec

class PropertyDescriptionViewSpec extends ViewBaseSpec {

  private val view         = applicationBuilder().injector().instanceOf[PropertyDescriptionView]
  private val formProvider = applicationBuilder().injector().instanceOf[PropertyDescriptionFormProvider]

  private val testIndex = 0

  "PropertyDescriptionView" - {

    "show correct title" in {
      doc(view(formProvider(), NormalMode, testIndex).body)
        .getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("propertyDescription.title")} - ${messages("service.name")} - GOV.UK"
    }

    behave like pageWithH1(view(formProvider(), NormalMode, testIndex), "propertyDescription.heading")

    "display textarea field" in {
      val textarea = doc(view(formProvider(), NormalMode, testIndex).body)
        .getElementById("value")

      assert(textarea != null, "Textarea field 'value' was not found")
      textarea.tagName() mustBe "textarea"
    }

    behave like pageWithSubmitButton(view(formProvider(), NormalMode, testIndex), "site.saveAndContinue")

    behave like pageWithErrors(
      view(formProvider().withError(FormError("value", "propertyDescription.error.required")), NormalMode, testIndex),
      "value",
      "propertyDescription.error.required"
    )
  }
}

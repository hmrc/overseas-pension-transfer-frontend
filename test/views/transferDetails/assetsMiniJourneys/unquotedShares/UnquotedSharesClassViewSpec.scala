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

package views.transferDetails.assetsMiniJourneys.unquotedShares

import forms.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesClassFormProvider
import models.NormalMode
import play.api.data.FormError
import views.html.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesClassView
import views.utils.ViewBaseSpec

class UnquotedSharesClassViewSpec extends ViewBaseSpec {

  private val view         = applicationBuilder().injector().instanceOf[UnquotedSharesClassView]
  private val formProvider = applicationBuilder().injector().instanceOf[UnquotedSharesClassFormProvider]

  private val testIndex = 0

  "UnquotedSharesClassView" - {

    "show correct title" in {
      doc(view(formProvider(), NormalMode, testIndex).body)
        .getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("unquotedSharesClass.title")} - ${messages("service.name")} - GOV.UK"
    }

    behave like pageWithH1(view(formProvider(), NormalMode, testIndex), "unquotedSharesClass.heading")

    behave like pageWithInputField(view(formProvider(), NormalMode, testIndex), "value", "unquotedSharesClass.heading")

    behave like pageWithSubmitButton(view(formProvider(), NormalMode, testIndex), "site.saveAndContinue")

    behave like pageWithErrors(
      view(formProvider().withError(FormError("value", "unquotedSharesClass.error.required")), NormalMode, testIndex),
      "value",
      "unquotedSharesClass.error.required"
    )
  }
}

/*
 * Copyright 2025 HM Revenue & Customs
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

import forms.DiscardTransferConfirmFormProvider
import play.api.data.FormError
import views.html.DiscardTransferConfirmView
import views.utils.ViewBaseSpec

class DiscardTransferConfirmViewSpec extends ViewBaseSpec {

  private val view         = applicationBuilder().injector().instanceOf[DiscardTransferConfirmView]
  private val formProvider = applicationBuilder().injector().instanceOf[DiscardTransferConfirmFormProvider]

  "DiscardTransferConfirmView" - {
    behave like pageWithTitle(view(formProvider()), "discardTransferConfirm.title")
    behave like pageWithH1(view(formProvider()), "discardTransferConfirm.heading")
    behave like pageWithBackLink(view(formProvider()))
    behave like pageWithRadioButtons(view(formProvider()), "site.yes", "site.no")
    behave like pageWithErrors(
      view(formProvider().withError(FormError.apply("discardTransfer", "discardTransferConfirm.error.required"))),
      "discardTransfer",
      "discardTransferConfirm.error.required"
    )
  }
}

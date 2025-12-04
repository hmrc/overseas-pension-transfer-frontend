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

package forms.viewandamend

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class ViewAmendSelectorFormProviderSpec extends StringFieldBehaviours {

  val form      = ViewAmendSelectorFormProvider.form()
  val fieldName = ViewAmendSelectorFormProvider.ViewOrAmend

  ".value" - {
    behave like mandatoryField(form, fieldName, FormError(fieldName, "viewAmend.error.required"))

    "bind" - {
      List("view", "amend") foreach {
        string =>
          s"value of $string" in {
            val result = form.bind(Map(fieldName -> string)).apply(fieldName)
            result.value mustBe Some(string)
          }
      }
    }
  }
}

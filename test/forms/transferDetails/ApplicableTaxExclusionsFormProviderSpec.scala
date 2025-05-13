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

package forms.transferDetails

import forms.ApplicableTaxExclusionsFormProvider
import forms.behaviours.CheckboxFieldBehaviours
import models.ApplicableTaxExclusions
import play.api.data.FormError

class ApplicableTaxExclusionsFormProviderSpec extends CheckboxFieldBehaviours {

  val form = new ApplicableTaxExclusionsFormProvider()()

  ".value" - {

    val fieldName   = "value"
    val requiredKey = "applicableTaxExclusions.error.required"

    behave like checkboxField[ApplicableTaxExclusions](
      form,
      fieldName,
      validValues  = ApplicableTaxExclusions.values,
      invalidError = FormError(s"$fieldName[0]", "error.invalid")
    )

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )
  }
}

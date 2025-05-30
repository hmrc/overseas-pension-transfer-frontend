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

package forms.qropsSchemeManagerDetails

import forms.behaviours.StringFieldBehaviours
import forms.mappings.Regex
import play.api.data.FormError

class SchemeManagersContactFormProviderSpec extends StringFieldBehaviours with Regex {

  val requiredKey = "schemeManagersContact.error.required"
  val lengthKey   = "schemeManagersContact.error.length"
  val maxLength   = 35

  val form = new SchemeManagersContactFormProvider()()

  ".value" - {

    val fieldName = "contactNumber"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsMatchingRegex(phoneNumberRegex, maybeMaxLength = Some(maxLength))
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength   = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}

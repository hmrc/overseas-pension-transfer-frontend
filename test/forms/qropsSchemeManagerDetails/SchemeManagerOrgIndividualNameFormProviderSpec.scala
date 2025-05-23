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

class SchemeManagerOrgIndividualNameFormProviderSpec extends StringFieldBehaviours with Regex {

  val form = new SchemeManagerOrgIndividualNameFormProvider()()

  ".orgIndFirstName" - {

    val fieldName   = "orgIndFirstName"
    val requiredKey = "orgIndividualName.error.firstName.required"
    val lengthKey   = "orgIndividualName.error.firstName.length"
    val patternKey  = "orgIndividualName.error.firstName.pattern"
    val maxLength   = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsMatchingRegex(nameRegex, maybeMaxLength = Some(maxLength))
    )

    behave like fieldThatRejectsInvalidCharacters(
      form,
      fieldName,
      patternError   = FormError(fieldName, patternKey, Seq(nameRegex)),
      maybeMaxLength = Some(maxLength)
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

  ".orgIndLastName" - {

    val fieldName   = "orgIndLastName"
    val requiredKey = "orgIndividualName.error.lastName.required"
    val lengthKey   = "orgIndividualName.error.lastName.length"
    val patternKey  = "orgIndividualName.error.lastName.pattern"
    val maxLength   = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsMatchingRegex(nameRegex, maybeMaxLength = Some(maxLength))
    )

    behave like fieldThatRejectsInvalidCharacters(
      form,
      fieldName,
      patternError   = FormError(fieldName, patternKey, Seq(nameRegex)),
      maybeMaxLength = Some(maxLength)
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

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
import models.PersonName
import play.api.data.FormError

class SchemeManagersNameFormProviderSpec extends StringFieldBehaviours with Regex {

  val form = new SchemeManagersNameFormProvider()()

  ".schemeManagersFirstName" - {

    val fieldName   = "schemeManagersFirstName"
    val requiredKey = "schemeManagersName.error.firstName.required"
    val lengthKey   = "schemeManagersName.error.firstName.length"
    val patternKey  = "schemeManagersName.error.firstName.pattern"
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

  ".schemeManagerLastNight" - {

    val fieldName   = "schemeManagersLastName"
    val requiredKey = "schemeManagersName.error.lastName.required"
    val lengthKey   = "schemeManagersName.error.lastName.length"
    val patternKey  = "schemeManagersName.error.lastName.pattern"
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

  "SchemeManagersNameFormProvider" - {

    "must allow leading and trailing spaces and trim them on binding" in {
      val result = form.bind(
        Map(
          "schemeManagersFirstName" -> "  Jane  ",
          "schemeManagersLastName"  -> "  Doe  "
        )
      )

      result.errors mustBe empty

      val PersonName(firstName, lastName) = result.value.value

      firstName mustBe "Jane"
      lastName mustBe "Doe"
    }

    "must handle names with spaces, allow leading and trailing spaces and trim them on binding" in {
      val result = form.bind(
        Map(
          "schemeManagersFirstName" -> "  Jimmy John  ",
          "schemeManagersLastName"  -> "  Doe Ray Mee  "
        )
      )

      result.errors mustBe empty

      val PersonName(firstName, lastName) = result.value.value

      firstName mustBe "Jimmy John"
      lastName mustBe "Doe Ray Mee"
    }
  }
}

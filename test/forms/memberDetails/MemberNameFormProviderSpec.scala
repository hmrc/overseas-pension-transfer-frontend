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

package forms.memberDetails

import forms.behaviours.StringFieldBehaviours
import forms.mappings.Regex
import models.PersonName
import play.api.data.FormError

class MemberNameFormProviderSpec extends StringFieldBehaviours with Regex {

  val form = new MemberNameFormProvider()()

  ".memberFirstName" - {

    val fieldName   = "memberFirstName"
    val requiredKey = "memberName.error.memberFirstName.required"
    val lengthKey   = "memberName.error.memberFirstName.length"
    val patternKey  = "memberName.error.memberFirstName.pattern"
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

  ".memberLastName" - {

    val fieldName   = "memberLastName"
    val requiredKey = "memberName.error.memberLastName.required"
    val lengthKey   = "memberName.error.memberLastName.length"
    val patternKey  = "memberName.error.memberLastName.pattern"
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

  "MemberNameFormProvider" - {

    "must allow leading and trailing spaces and trim them on binding" in {
      val result = form.bind(
        Map(
          "memberFirstName" -> "  Jane  ",
          "memberLastName"  -> "  Doe  "
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
          "memberFirstName" -> "  Jimmy John  ",
          "memberLastName"  -> "  Doe Ray Mee  "
        )
      )

      result.errors mustBe empty

      val PersonName(firstName, lastName) = result.value.value

      firstName mustBe "Jimmy John"
      lastName mustBe "Doe Ray Mee"
    }
  }
}

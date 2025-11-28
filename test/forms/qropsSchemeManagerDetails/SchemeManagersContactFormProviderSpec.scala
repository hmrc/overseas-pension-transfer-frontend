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
import play.api.data.FormError

class SchemeManagersContactFormProviderSpec extends StringFieldBehaviours {

  private val requiredKey = "schemeManagersContact.error.required"
  private val lengthKey   = "schemeManagersContact.error.length"
  private val patternKey  = "schemeManagersContact.error.pattern"
  private val maxLength   = 35

  private val form      = new SchemeManagersContactFormProvider()()
  private val fieldName = "contactNumber"

  ".contactNumber" - {

    "must bind valid international phone numbers" in {

      val validNumbers = Seq(
        "+44 7911 123 456",
        "07911123456",
        "(+1) 202-555-0123",
        "+33123456789"
      )

      validNumbers.foreach { number =>
        val result = form.bind(Map(fieldName -> number))

        withClue(s"For input '$number': ") {
          result.errors mustBe empty
        }
      }
    }

    "must reject invalid phone numbers" in {

      val invalidNumbers = Seq(
        "abc123",
        "+",
        "+++++++",
        "123",
        "0000000000"
      )

      invalidNumbers.foreach { number =>
        val result = form.bind(Map(fieldName -> number))

        withClue(s"For input '$number': ") {
          result.errors.map(_.message) must contain(patternKey)
        }
      }
    }

    "must strip whitespace before validating and binding" in {

      val input  = "   +44 7911 123456   "
      val result = form.bind(Map(fieldName -> input))

      result.errors mustBe empty
      result.value.value mustBe "+447911123456"
    }

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

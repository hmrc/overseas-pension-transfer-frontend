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
import play.api.data.FormError

import scala.util.Random

class MemberNinoFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "memberNino.error.required"
  val lengthKey   = "memberNino.error.length"
  val patternKey  = "memberNino.error.pattern"
  val maxLength   = 9

  val ninoRegex = "^[A-Z]{2}\\d{6}[A-Z]$"

  val form = new MemberNinoFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsMatchingRegex(ninoRegex, maybeMaxLength = Some(maxLength))
    )

    behave like fieldThatRejectsInvalidCharacters(
      form,
      fieldName,
      patternError = FormError(fieldName, patternKey, Seq(ninoRegex))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  "MemberNinoFormProvider" - {

    "must allow combinations of whitespace and strip them on binding" in {

      val ninoParts = Seq(
        "AA",
        f"${Random.nextInt(100)}%02d",
        f"${Random.nextInt(100)}%02d",
        f"${Random.nextInt(100)}%02d",
        "C"
      )

      val inputs = Seq(
        ninoParts.mkString(""),
        ninoParts.mkString(" ", "", " "),
        ninoParts.mkString(" ")
      )

      val expected = ninoParts.mkString("")

      inputs.foreach { input =>
        val result = form.bind(Map("value" -> input))

        withClue(s"For input '$input': ") {
          result.errors mustBe empty
          result.value.value mustBe expected
        }
      }
    }
  }
}

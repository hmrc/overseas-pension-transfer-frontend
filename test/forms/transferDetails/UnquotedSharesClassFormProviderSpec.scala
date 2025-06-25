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

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class UnquotedSharesClassFormProviderSpec extends StringFieldBehaviours {

  val form = new UnquotedSharesClassFormProvider()()

  val classRegex: String = "^[A-Za-zÀ-ÖØ-öø-ÿ]+(?:[ '-][A-Za-zÀ-ÖØ-öø-ÿ]+)*$"

  ".unquotedShareClass" - {

    val requiredKey = "unquotedSharesClass.error.required"
    val lengthKey   = "unquotedSharesClass.error.length"
    val patternKey  = "unquotedSharesClass.error.pattern"
    val maxLength   = 160

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
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

    behave like fieldThatRejectsInvalidCharacters(
      form,
      fieldName,
      patternError   = FormError(fieldName, patternKey, Seq(classRegex)),
      maybeMaxLength = Some(maxLength)
    )
  }
}

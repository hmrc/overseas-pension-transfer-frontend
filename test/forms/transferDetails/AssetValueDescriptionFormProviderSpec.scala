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

class AssetValueDescriptionFormProviderSpec extends StringFieldBehaviours {

  val requiredKey              = "assetValueDescription.error.required"
  val lengthKey                = "assetValueDescription.error.length"
  val patternKey               = "assetValueDescription.error.pattern"
  val maxLength                = 160
  val descriptionRegex: String = """^[A-Za-z0-9 \-,.&'/]+$"""

  val form = new AssetValueDescriptionFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsMatchingRegex(descriptionRegex, maybeMaxLength = Some(maxLength))
    )

    behave like fieldThatRejectsInvalidCharacters(
      form,
      fieldName,
      patternError = FormError(fieldName, patternKey, Seq(descriptionRegex))
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

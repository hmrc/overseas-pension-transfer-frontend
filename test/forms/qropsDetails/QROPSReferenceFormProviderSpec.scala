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

package forms.qropsDetails

import forms.behaviours.StringFieldBehaviours
import forms.mappings.Regex
import play.api.data.FormError

class QROPSReferenceFormProviderSpec extends StringFieldBehaviours with Regex {

  val requiredKey = "qropsReference.error.required"
  val invalidKey  = "qropsReference.error.invalid"
  val maxLength   = 11

  val form = new QROPSReferenceFormProvider()()

  ".value" - {

    val fieldName = "qropsRef"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsMatchingRegex(qropsRefRegex)
    )

    behave like fieldThatRejectsInvalidCharacters(
      form,
      fieldName,
      patternError = FormError(fieldName, invalidKey, Seq(qropsRefRegex))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}

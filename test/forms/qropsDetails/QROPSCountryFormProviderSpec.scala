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
import play.api.data.FormError

class QROPSCountryFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "qropsCountry.error.required"
  val lengthKey   = "qropsCountry.error.length"
  val maxLength   = 100

  val form = new QROPSCountryFormProvider()()

  ".value" - {

    val fieldName = "countryCode"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}

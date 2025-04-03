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

package forms

import forms.behaviours.StringFieldBehaviours
import forms.mappings.Regex
import play.api.data.FormError

class MembersCurrentAddressFormProviderSpec extends StringFieldBehaviours with Regex {

  private val memberName = "Undefined Undefined"
  private val form       = new MembersCurrentAddressFormProvider()(memberName)

  ".addressLine1" - {

    val fieldName   = "addressLine1"
    val requiredKey = "membersCurrentAddress.error.addressLine1.required"
    val lengthKey   = "membersCurrentAddress.error.addressLine1.length"
    val patternKey  = "membersCurrentAddress.error.addressLine1.pattern"
    val maxLength   = 35

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
      requiredError = FormError(fieldName, requiredKey, Seq(memberName))
    )

    behave like fieldThatRejectsInvalidCharacters(
      form,
      fieldName,
      patternError = FormError(fieldName, patternKey, Seq(addressLinesRegex)),
      Option(maxLength)
    )
  }

  ".addressLine2" - {

    val fieldName   = "addressLine2"
    val requiredKey = "membersCurrentAddress.error.addressLine2.required"
    val lengthKey   = "membersCurrentAddress.error.addressLine2.length"
    val patternKey  = "membersCurrentAddress.error.addressLine2.pattern"
    val maxLength   = 35

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
      requiredError = FormError(fieldName, requiredKey, Seq(memberName))
    )

    behave like fieldThatRejectsInvalidCharacters(
      form,
      fieldName,
      patternError = FormError(fieldName, patternKey, Seq(addressLinesRegex)),
      Option(maxLength)
    )
  }

  ".addressLine3" - {

    val fieldName  = "addressLine3"
    val lengthKey  = "membersCurrentAddress.error.addressLine3.length"
    val patternKey = "membersCurrentAddress.error.addressLine3.pattern"
    val maxLength  = 35

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

    behave like optionalField(
      form,
      fieldName
    )

    behave like fieldThatRejectsInvalidCharacters(
      form,
      fieldName,
      patternError = FormError(fieldName, patternKey, Seq(addressLinesRegex)),
      Option(maxLength)
    )
  }

  "addressLine4" - {

    val fieldName  = "addressLine4"
    val lengthKey  = "membersCurrentAddress.error.addressLine4.length"
    val patternKey = "membersCurrentAddress.error.addressLine4.pattern"
    val maxLength  = 35

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

    behave like optionalField(
      form,
      fieldName
    )

    behave like fieldThatRejectsInvalidCharacters(
      form,
      fieldName,
      patternError = FormError(fieldName, patternKey, Seq(addressLinesRegex)),
      Option(maxLength)
    )
  }

  "countryCode" - {

    val fieldName   = "countryCode"
    val requiredKey = "membersCurrentAddress.error.countryCode.required"

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  "postcode" - {

    val fieldName  = "postcode"
    val lengthKey  = "membersCurrentAddress.error.postcode.length"
    val patternKey = "membersCurrentAddress.error.postcode.pattern"
    val maxLength  = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsMatchingRegex(internationalPostcodeRegex, maybeMaxLength = Some(maxLength))
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength   = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like optionalField(
      form,
      fieldName
    )

    behave like fieldThatRejectsInvalidCharacters(
      form,
      fieldName,
      patternError = FormError(fieldName, patternKey, Seq(internationalPostcodeRegex)),
      Option(maxLength)
    )
  }

  "poBox" - {

    val fieldName  = "poBox"
    val lengthKey  = "membersCurrentAddress.error.poBox.length"
    val patternKey = "membersCurrentAddress.error.poBox.pattern"
    val maxLength  = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsMatchingRegex(poBoxRegex, maybeMaxLength = Some(maxLength))
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength   = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like optionalField(
      form,
      fieldName
    )

    behave like fieldThatRejectsInvalidCharacters(
      form,
      fieldName,
      patternError = FormError(fieldName, patternKey, Seq(poBoxRegex)),
      Option(maxLength)
    )
  }
}

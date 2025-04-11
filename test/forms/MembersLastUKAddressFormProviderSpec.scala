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

import base.SpecBase
import forms.behaviours.StringFieldBehaviours
import forms.mappings.Regex
import pages.MembersLastUKAddressPage
import play.api.data.FormError
import models.address._

class MembersLastUKAddressFormProviderSpec extends StringFieldBehaviours with SpecBase with Regex {

  val form = new MembersLastUKAddressFormProvider()()

  private val memberName = testDisplayRequest.memberName

  ".addressLine1" - {

    val fieldName   = "addressLine1"
    val requiredKey = "membersLastUKAddress.error.addressLine1.required"
    val lengthKey   = "membersLastUKAddress.error.addressLine1.length"
    val patternKey  = "membersLastUKAddress.error.addressLine1.pattern"
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
    val requiredKey = "membersLastUKAddress.error.addressLine2.required"
    val lengthKey   = "membersLastUKAddress.error.addressLine2.length"
    val patternKey  = "membersLastUKAddress.error.addressLine2.pattern"
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

  "addressLine3" - {

    val fieldName = "addressLine3"
    val lengthKey = "membersLastUKAddress.error.addressLine3.length"
    val maxLength = 35

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
  }

  "addressLine4" - {

    val fieldName = "addressLine4"
    val lengthKey = "membersLastUKAddress.error.addressLine4.length"
    val maxLength = 35

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
  }

  "postcode" - {

    val fieldName   = "postcode"
    val lengthKey   = "membersLastUKAddress.error.postcode.length"
    val requiredKey = "membersLastUKAddress.error.postcode.required"
    val maxLength   = 16

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsMatchingRegex(postcodeRegex)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength   = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )
  }
}

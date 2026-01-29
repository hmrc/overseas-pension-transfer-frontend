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

package forms.transferDetails.assetsMiniJourneys.property

import base.SpecBase
import forms.behaviours.StringFieldBehaviours
import forms.mappings.Regex
import play.api.data.FormError

class PropertyAddressFormProviderOLDSpec extends StringFieldBehaviours with Regex with SpecBase {

  private val form = new PropertyAddressFormProvider()(false)

  ".addressLine1" - {

    val fieldName   = "addressLine1"
    val requiredKey = "propertyAddress.error.addressLine1.required"
    val lengthKey   = "common.addressInput.error.addressLine1.length"
    val patternKey  = "common.addressInput.error.addressLine1.pattern"
    val maxLength   = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
        .suchThat(_.trim.nonEmpty)
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
      patternError = FormError(fieldName, patternKey, Seq(addressLinesRegex)),
      Option(maxLength)
    )
  }

  ".addressLine2" - {

    val fieldName   = "addressLine2"
    val requiredKey = "propertyAddress.error.addressLine2.required"
    val lengthKey   = "common.addressInput.error.addressLine2.length"
    val patternKey  = "common.addressInput.error.addressLine2.pattern"
    val maxLength   = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
        .suchThat(_.trim.nonEmpty)
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
      patternError = FormError(fieldName, patternKey, Seq(addressLinesRegex)),
      Option(maxLength)
    )
  }

  ".addressLine3" - {

    val fieldName  = "addressLine3"
    val lengthKey  = "common.addressInput.error.addressLine3.length"
    val patternKey = "common.addressInput.error.addressLine3.pattern"
    val maxLength  = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
        .suchThat(_.trim.nonEmpty)
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
    val lengthKey  = "common.addressInput.error.addressLine4.length"
    val patternKey = "common.addressInput.error.addressLine4.pattern"
    val maxLength  = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
        .suchThat(_.trim.nonEmpty)
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
    val requiredKey = "common.addressInput.error.countryCode.required"

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  "postcode" - {

    val fieldName  = "postcode"
    val lengthKey  = "common.addressInput.error.postcode.length"
    val patternKey = "common.addressInput.error.postcode.pattern"
    val maxLength  = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsMatchingRegex(internationalPostcodeRegex, maybeMaxLength = Some(maxLength))
        .suchThat(_.trim.nonEmpty)
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

  "PropertyAddressFormProvider" - {

    "must allow leading and trailing spaces and trim them on binding" in {

      val result = form.bind(
        Map(
          "addressLine1" -> "  10 Downing Street  ",
          "addressLine2" -> "  Westminster  ",
          "addressLine3" -> "  London  ",
          "addressLine4" -> "  Greater London  ",
          "countryCode"  -> "GB",
          "postcode"     -> "  SW1A 2AA  "
        )
      )

      result.errors mustBe empty

      val bound = result.value.value

      bound.addressLine1 mustBe "10 Downing Street"
      bound.addressLine2 mustBe "Westminster"
      bound.addressLine3.value mustBe "London"
      bound.addressLine4.value mustBe "Greater London"
      bound.countryCode mustBe "GB"
      bound.postcode.value mustBe "SW1A2AA"
    }
  }
}

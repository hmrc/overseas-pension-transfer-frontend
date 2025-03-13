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

import forms.behaviours.OptionFieldBehaviours
import models.{AddressRecord, Country, RecordSet, UkAddress}
import play.api.data.FormError

class MemberSelectLastUkAddressFormProviderSpec extends OptionFieldBehaviours {

  private val addressRecords =
    RecordSet(addresses =
      Seq(
        AddressRecord(
          id      = "GB990091234514",
          address = UkAddress(
            lines       = List("2 Other Place", "Some District"),
            town        = "Anytown",
            rawPostCode = "ZZ1 1ZZ",
            rawCountry  = Country(code = "GB", name = "United Kingdom")
          )
        ),
        AddressRecord(
          id      = "GB990091234515",
          address = UkAddress(
            lines       = List("3 Other Place", "Some District"),
            town        = "Anytown",
            rawPostCode = "ZZ1 1ZZ",
            rawCountry  = Country(code = "GB", name = "United Kingdom")
          )
        )
      )
    )

  private val validIds = addressRecords.addresses.map(_.id)

  val form = new MemberSelectLastUkAddressFormProvider()(validIds)

  ".value" - {

    val fieldName   = "value"
    val requiredKey = "memberSelectLastUkAddress.error.required"
    val invalidKey  = "memberSelectLastUkAddress.error.invalid"

    behave like optionsField[String](
      form,
      fieldName,
      validValues  = validIds,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}

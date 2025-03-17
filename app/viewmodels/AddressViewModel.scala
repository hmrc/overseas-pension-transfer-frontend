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

package viewmodels

import models.{Address, RecordSet}
import uk.gov.hmrc.govukfrontend.views.Aliases.{RadioItem, Text}

case class AddressViewModel(
    line1: String,
    line2: String,
    line3: Option[String],
    city: Option[String],
    country: Option[String],
    postcode: Option[String]
  )

object AddressViewModel {

  def fromAddress(address: Address): AddressViewModel = {
    AddressViewModel(
      line1    = address.line1,
      line2    = address.line2,
      line3    = address.line3,
      city     = address.city,
      postcode = address.postcode,
      country  = address.country
    )
  }

  def formatAddressAsLines(vm: AddressViewModel, ukMode: Boolean = false): List[String] = {
    def toOption[A](a: A)(implicit ev: AddressField[A]): Option[String] = ev.toOption(a)
    List(
      toOption(vm.line1),
      toOption(vm.line2),
      toOption(vm.line3),
      toOption(vm.city),
      toOption(vm.postcode),
      toOption(vm.country).filterNot(_ => ukMode)
    ).flatten
  }

  def formatAddressAsString(vm: AddressViewModel): String =
    formatAddressAsLines(vm).mkString(", ")

  def formatAddressAsStringExcludingCountry(vm: AddressViewModel): String =
    formatAddressAsLines(vm, ukMode = true).mkString(", ")

  def addressRadios(addressRecords: RecordSet): Seq[RadioItem] =
    addressRecords.addresses.zipWithIndex.map { case (addressRecord, index) =>
      val vm               = AddressViewModel.fromAddress(addressRecord.address)
      val formattedAddress = formatAddressAsStringExcludingCountry(vm)

      RadioItem(
        content = Text(formattedAddress),
        value   = Some(addressRecord.id),
        id      = Some(s"value_$index")
      )
    }

}

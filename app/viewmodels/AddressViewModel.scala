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

import models.address._
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.Aliases.{RadioItem, Text}

import scala.language.implicitConversions

case class AddressViewModel(
    addressLine1: String,
    addressLine2: String,
    addressLine3: Option[String],
    addressLine4: Option[String],
    addressLine5: Option[String],
    country: String,
    ukPostCode: Option[String],
    poBox: Option[String]
  )

object AddressViewModel {
  private def toOption[A](a: A)(implicit ev: AddressField[A]): Option[String] = ev.toOption(a)

  implicit def fromAddress(address: Address): AddressViewModel = {
    AddressViewModel(
      addressLine1 = address.addressLine1,
      addressLine2 = address.addressLine2,
      addressLine3 = address.addressLine3,
      addressLine4 = address.addressLine4,
      addressLine5 = address.addressLine5,
      ukPostCode   = address.postcode,
      country      = address.country.name,
      poBox        = address.poBoxNumber
    )
  }

  def formatAddressAsLines(vm: AddressViewModel, ukMode: Boolean = false): List[String] = {
    List(
      toOption(vm.addressLine1),
      toOption(vm.addressLine2),
      toOption(vm.addressLine3),
      toOption(vm.addressLine4),
      if (vm.poBox.isEmpty) toOption(vm.addressLine5) else None,
      toOption(vm.country).filterNot(_ => ukMode),
      toOption(vm.ukPostCode),
      toOption(vm.poBox)
    ).flatten.filterNot(_.isBlank)
  }

  def formatAddressAsString(vm: AddressViewModel): String =
    formatAddressAsLines(vm).mkString(", ")

  def formatAddressAsStringExcludingCountry(vm: AddressViewModel): String =
    formatAddressAsLines(vm, ukMode = true).mkString(", ")

  def addressRadios(idsWithAddresses: Seq[(String, Address)]): Seq[RadioItem] =
    idsWithAddresses.zipWithIndex.map { case ((id, address), index) =>
      val vm        = AddressViewModel.fromAddress(address)
      val formatted = formatAddressAsStringExcludingCountry(vm)

      RadioItem(
        content = Text(formatted),
        value   = Some(id),
        id      = Some(s"value_$index")
      )
    }

  def formatAddressWithLineBreaks(vm: AddressViewModel, ukMode: Boolean): Html =
    HtmlFormat.fill {
      val lines = formatAddressAsLines(vm, ukMode)
      lines.zipWithIndex.map {
        case (line, idx) =>
          val isLast = idx == lines.length - 1
          if (isLast) Html(line) else Html(s"$line<br>")
      }
    }
}

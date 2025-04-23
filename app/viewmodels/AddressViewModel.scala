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
    line1: String,
    line2: String,
    line3: Option[String],
    line4: Option[String],
    line5: Option[String],
    country: String,
    postcode: Option[String],
    poBox: Option[String]
  )

object AddressViewModel {
  private def toOption[A](a: A)(implicit ev: AddressField[A]): Option[String] = ev.toOption(a)

  implicit def fromAddress(address: Address): AddressViewModel = {
    AddressViewModel(
      line1    = address.line1,
      line2    = address.line2,
      line3    = address.line3,
      line4    = address.line4,
      line5    = address.line5,
      postcode = address.postcode,
      country  = address.country.name,
      poBox    = address.poBox
    )
  }

  def formatAddressAsLines(vm: AddressViewModel, ukMode: Boolean = false): List[String] = {
    List(
      toOption(vm.line1),
      toOption(vm.line2),
      toOption(vm.line3),
      toOption(vm.line4),
      toOption(vm.line5),
      toOption(vm.postcode),
      toOption(vm.country).filterNot(_ => ukMode),
      toOption(vm.poBox)
    ).flatten
  }

  def formatAddressAsString(vm: AddressViewModel): String =
    formatAddressAsLines(vm).mkString(", ")

  def formatAddressAsStringExcludingCountry(vm: AddressViewModel): String =
    formatAddressAsLines(vm, ukMode = true).mkString(", ")

  def addressRadios(addresses: Seq[FoundAddress]): Seq[RadioItem] =
    addresses.zipWithIndex.map { case (fa, index) =>
      val vm               = AddressViewModel.fromAddress(fa.address)
      val formattedAddress = formatAddressAsStringExcludingCountry(vm)

      RadioItem(
        content = Text(formattedAddress),
        value   = Some(fa.id),
        id      = Some(s"value_$index")
      )
    }

  def formatAddressWithLineBreaks(vm: AddressViewModel, ukMode: Boolean): Html =
    HtmlFormat.fill(
      for (line <- formatAddressAsLines(vm, ukMode))
        yield Html(s"$line<br>")
    )
}

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

package models.address

import play.api.libs.json.{__, JsString, JsonValidationError, Reads, Writes}

sealed trait AddressType { def name: String }

object AddressType {
  case object MembersLastUK       extends AddressType { val name = "MembersLastUKAddress" }
  case object MembersLookupLastUK extends AddressType { val name = "MembersLookupLastUkAddress" }
  case object MembersCurrent      extends AddressType { val name = "MembersCurrentAddress" }
  case object QROPS               extends AddressType { val name = "QROPSAddress" }
  case object SchemeManager       extends AddressType { val name = "SchemeManagersAddress" }
  case object Property            extends AddressType { val name = "PropertyAddress" }

  val all: Seq[AddressType] = Seq(MembersLastUK, MembersLookupLastUK, MembersCurrent, QROPS, SchemeManager, Property)

  def fromName(name: String): Option[AddressType] = all.find(_.name == name)

  implicit val reads: Reads[AddressType] =
    __.read[String].map(fromName).collect(JsonValidationError("Unknown address type")) {
      case Some(t) => t
    }

  implicit val writes: Writes[AddressType] = Writes(t => JsString(t.name))
}

abstract class AddressWithType(val addressType: AddressType) extends Address

case class MembersCurrentAddress(base: BaseAddress)      extends AddressWithType(AddressType.MembersCurrent)
case class QROPSAddress(base: BaseAddress)               extends AddressWithType(AddressType.QROPS)
case class SchemeManagersAddress(base: BaseAddress)      extends AddressWithType(AddressType.SchemeManager)
case class PropertyAddress(base: BaseAddress)            extends AddressWithType(AddressType.Property)
case class MembersLastUKAddress(base: BaseAddress)       extends AddressWithType(AddressType.MembersLastUK)
case class MembersLookupLastUKAddress(base: BaseAddress) extends AddressWithType(AddressType.MembersLookupLastUK)

object MembersLookupLastUKAddress {

  def fromAddressRecord(record: AddressRecord): MembersLookupLastUKAddress = {
    val raw   = record.address
    val lines = raw.lines :+ raw.town padTo (5, "")
    MembersLookupLastUKAddress(
      BaseAddress(
        line1    = lines.headOption.getOrElse(""),
        line2    = lines.lift(1).getOrElse(""),
        line3    = lines.lift(2).filter(_.nonEmpty),
        line4    = lines.lift(3).filter(_.nonEmpty),
        line5    = lines.lift(4).filter(_.nonEmpty),
        country  = raw.country,
        postcode = Some(raw.postcode),
        poBox    = record.poBox
      )
    )
  }
}

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

package models

import play.api.libs.functional.syntax._
import play.api.libs.json._

sealed trait FoundAddressResponse

object FoundAddressResponse {
  implicit val foundAddressSetWrites: Writes[FoundAddressSet] = Json.writes[FoundAddressSet]
  implicit val noAddressFoundWrites: Writes[NoAddressFound]   = Json.writes[NoAddressFound]
  implicit val foundAddressSetReads: Reads[FoundAddressSet]   = Json.reads[FoundAddressSet]
  implicit val noAddressFoundReads: Reads[NoAddressFound]     = Json.reads[NoAddressFound]

  implicit val writes: Writes[FoundAddressResponse] = Writes[FoundAddressResponse] {
    case fas: FoundAddressSet => Json.toJson(fas)(foundAddressSetWrites)
    case naf: NoAddressFound  => Json.toJson(naf)(noAddressFoundWrites)
  }

  implicit val foundAddressResponseReads: Reads[FoundAddressResponse] = (
    (__ \ "searchedPostcode").read[String] and
      (__ \ "addresses").readNullable[Seq[FoundAddress]]
  )((postcode, maybeAddresses) =>
    maybeAddresses match {
      case Some(addresses) if addresses.nonEmpty => FoundAddressSet(postcode, addresses)
      case _                                     => NoAddressFound(postcode)
    }
  )

  implicit object AddressOrdering extends Ordering[FoundAddress] {
    def compare(a: FoundAddress, b: FoundAddress): Int = a.id compare b.id
  }

  def fromRecordSet(searchedPostcode: String, rs: RecordSet): FoundAddressResponse =
    if (rs.addresses.nonEmpty) {
      FoundAddressSet(searchedPostcode, rs.addresses.map(ar => FoundAddress(ar.id, UkAddress.fromRawAddress(ar.address))).sorted)
    } else {
      NoAddressFound(searchedPostcode)
    }
}

case class FoundAddress(id: String, address: UkAddress)

object FoundAddress {
  implicit val format: OFormat[FoundAddress] = Json.format
}

case class FoundAddressSet(searchedPostcode: String, addresses: Seq[FoundAddress]) extends FoundAddressResponse

case class NoAddressFound(searchedPostcode: String) extends FoundAddressResponse

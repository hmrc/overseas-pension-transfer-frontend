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

import play.api.libs.json._

case class MembersLastUKAddress(
    addressLine1: String,
    addressLine2: String,
    addressLine3: Option[String],
    addressLine4: Option[String],
    ukPostCode: String
  ) extends Address {
  val addressLine5: Option[Nothing] = None
  val country: Country              = Countries.UK
  val postcode: Option[String]      = Some(ukPostCode)
  val poBoxNumber: Option[String]   = None
}

object MembersLastUKAddress {
  implicit val format: OFormat[MembersLastUKAddress] = Json.format

  def fromAddress(address: Address): MembersLastUKAddress = {
    MembersLastUKAddress(
      addressLine1 = address.addressLine1,
      addressLine2 = address.addressLine2,
      addressLine3 = address.addressLine3,
      addressLine4 = address.addressLine4,
      ukPostCode   = address.postcode.getOrElse("")
    )
  }
}

case class MembersLookupLastUkAddress(
    addressLine1: String,
    addressLine2: String,
    addressLine3: Option[String],
    addressLine4: Option[String],
    country: Country,
    ukPostCode: Option[String],
    poBoxNumber: Option[String]
  ) extends Address {
  val addressLine5: Option[String] = None
  val postcode: Option[String]     = ukPostCode
}

object MembersLookupLastUkAddress {
  implicit val format: OFormat[MembersLookupLastUkAddress] = Json.format

  def fromAddressRecord(record: AddressRecord): MembersLookupLastUkAddress = {
    val raw = record.address

    MembersLookupLastUkAddress(
      addressLine1 = raw.lines.headOption.getOrElse(""),
      addressLine2 = raw.lines.lift(1).getOrElse(""),
      addressLine3 = raw.lines.lift(2),
      addressLine4 = raw.lines.lift(3),
      ukPostCode   = Some(raw.postcode),
      country      = raw.country,
      poBoxNumber  = record.poBox
    )
  }

}

case class MembersCurrentAddress(
    addressLine1: String,
    addressLine2: String,
    addressLine3: Option[String],
    addressLine4: Option[String],
    country: Country,
    ukPostCode: Option[String],
    poBoxNumber: Option[String]
  ) extends Address {
  val addressLine5: Option[String] = None
  val postcode: Option[String]     = ukPostCode
}

object MembersCurrentAddress {
  implicit val format: OFormat[MembersCurrentAddress] = Json.format
}

case class QROPSAddress(
    addressLine1: String,
    addressLine2: String,
    addressLine3: Option[String],
    addressLine4: Option[String],
    addressLine5: Option[String],
    country: Country
  ) extends Address {
  val countryCode: Country              = country
  val poBoxNumber: Option[String]       = None
  override val postcode: Option[String] = None
}

object QROPSAddress {
  implicit val format: OFormat[QROPSAddress] = Json.format
}

case class SchemeManagersAddress(
    addressLine1: String,
    addressLine2: String,
    addressLine3: Option[String],
    addressLine4: Option[String],
    addressLine5: Option[String],
    country: Country
  ) extends Address {
  val poBoxNumber: Option[String]       = None
  override val postcode: Option[String] = None
}

object SchemeManagersAddress {
  implicit val format: OFormat[SchemeManagersAddress] = Json.format

}

case class PropertyAddress(
    addressLine1: String,
    addressLine2: String,
    addressLine3: Option[String],
    addressLine4: Option[String],
    addressLine5: Option[String],
    country: Country,
    ukPostCode: Option[String]
  ) extends Address {

  val town: Option[String]        = addressLine3
  val county: Option[String]      = addressLine4
  val postcode: Option[String]    = ukPostCode
  val poBoxNumber: Option[String] = addressLine5
}

object PropertyAddress {
  implicit val format: OFormat[PropertyAddress] = Json.format
}

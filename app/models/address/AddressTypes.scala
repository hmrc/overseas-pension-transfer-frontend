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
    rawPostcode: String
  ) extends Address {
  val line1: String            = addressLine1
  val line2: String            = addressLine2
  val line3: Option[String]    = Option(addressLine3.getOrElse(""))
  val line4: Option[String]    = Option(addressLine4.getOrElse(""))
  val line5: Option[Nothing]   = None
  val country: Country         = Countries.UK
  val postcode: Option[String] = Some(rawPostcode)
  val poBox: Option[String]    = None
}

object MembersLastUKAddress {
  implicit val format: OFormat[MembersLastUKAddress] = Json.format

  def fromAddress(address: Address): MembersLastUKAddress = {
    MembersLastUKAddress(
      addressLine1 = address.line1,
      addressLine2 = address.line2,
      addressLine3 = address.line3,
      addressLine4 = address.line4,
      rawPostcode  = address.postcode.getOrElse("")
    )
  }
}

case class MembersLookupLastUkAddress(
    line1: String,
    line2: String,
    line3: Option[String],
    line4: Option[String],
    country: Country,
    postcode: Option[String],
    poBox: Option[String]
  ) extends Address {
  val line5: Option[Nothing] = None
}

object MembersLookupLastUkAddress {
  implicit val format: OFormat[MembersLookupLastUkAddress] = Json.format

  def fromAddressRecord(record: AddressRecord): MembersLookupLastUkAddress = {
    val raw = record.address

    MembersLookupLastUkAddress(
      line1    = raw.lines.headOption.getOrElse(""),
      line2    = raw.lines.lift(1).getOrElse(""),
      line3    = raw.lines.lift(2),
      line4    = raw.lines.lift(3),
      postcode = Some(raw.postcode),
      country  = raw.country,
      poBox    = record.poBox
    )
  }

}

case class MembersCurrentAddress(
    addressLine1: String,
    addressLine2: String,
    addressLine3: Option[String],
    addressLine4: Option[String],
    country: Country,
    postcode: Option[String],
    poBox: Option[String]
  ) extends Address {
  val line1: String            = addressLine1
  val line2: String            = addressLine2
  val line3: Option[String]    = addressLine3
  val line4: Option[String]    = addressLine4
  val line5: Option[String]    = None
  val countryCode: Country     = country
  val postCode: Option[String] = postcode
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
  val line1: String                     = addressLine1
  val line2: String                     = addressLine2
  val line3: Option[String]             = addressLine3
  val line4: Option[String]             = addressLine4
  val line5: Option[String]             = addressLine5
  val countryCode: Country              = country
  val poBox: Option[String]             = None
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
  val line1: String                     = addressLine1
  val line2: String                     = addressLine2
  val line3: Option[String]             = addressLine3
  val line4: Option[String]             = addressLine4
  val line5: Option[String]             = addressLine5
  val countryCode: Country              = country
  val poBox: Option[String]             = None
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
    country: Country,
    postcode: Option[String]
  ) extends Address {
  val line1: String                  = addressLine1
  val line2: String                  = addressLine2
  val line3: Option[String]          = addressLine3
  val line4: Option[String]          = addressLine4
  val line5: Option[Nothing]         = None
  val countryCode: Country           = country
  val postCode: Option[String]       = postcode
  override val poBox: Option[String] = None
}

object PropertyAddress {
  implicit val format: OFormat[PropertyAddress] = Json.format
}

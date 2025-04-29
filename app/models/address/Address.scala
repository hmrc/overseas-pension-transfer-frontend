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

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._

sealed trait Address {
  val line1: String
  val line2: String
  val line3: Option[String]
  val line4: Option[String]
  val line5: Option[String]
  val country: Country
  val postcode: Option[String]
  val poBox: Option[String]
}

object Address {

  implicit val reads: Reads[Address] =
    (__ \ "type").read[String].flatMap {
      case "MembersLastUKAddress" =>
        MembersLastUKAddress.reads.widen[Address]

      case "MembersLookupLastUkAddress" =>
        MembersLookupLastUkAddress.reads.widen[Address]

      case "MembersCurrentAddress" =>
        MembersCurrentAddress.reads.widen[Address]

      case "QROPSAddress" =>
        QROPSAddress.reads.widen[Address]

      case other =>
        Reads(_ => JsError(s"Unknown Address type: $other"))
    }

  implicit val writes: OWrites[Address] = OWrites {
    case a: MembersLastUKAddress =>
      MembersLastUKAddress.writes.writes(a).as[JsObject] +
        ("type" -> JsString("MembersLastUKAddress"))

    case a: MembersLookupLastUkAddress =>
      MembersLookupLastUkAddress.writes.writes(a).as[JsObject] +
        ("type" -> JsString("MembersLookupLastUkAddress"))

    case a: MembersCurrentAddress =>
      MembersCurrentAddress.writes.writes(a).as[JsObject] +
        ("type" -> JsString("MembersCurrentAddress"))

    case a: QROPSAddress =>
      QROPSAddress.writes.writes(a).as[JsObject] +
        ("type" -> JsString("QROPSAddress"))
  }

  implicit val format: OFormat[Address] = OFormat(reads, writes)
}

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

  implicit val reads: Reads[MembersLastUKAddress] = (
    (__ \ "line1").read[String] and
      (__ \ "line2").read[String] and
      (__ \ "line3").readNullable[String] and
      (__ \ "line4").readNullable[String] and
      (__ \ "postcode").read[String]
  )(MembersLastUKAddress.apply _)

  implicit val writes: OWrites[MembersLastUKAddress] = OWrites[MembersLastUKAddress] { address =>
    Json.obj(
      "line1"    -> address.line1,
      "line2"    -> address.line2,
      "line3"    -> address.line3,
      "line4"    -> address.line4,
      "postcode" -> address.rawPostcode
    )
  }

  implicit val format: OFormat[MembersLastUKAddress] = OFormat(reads, writes)

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

  implicit val reads: Reads[MembersLookupLastUkAddress] = (
    (__ \ "line1").read[String] and
      (__ \ "line2").read[String] and
      (__ \ "line3").readNullable[String] and
      (__ \ "line4").readNullable[String] and
      (__ \ "country").read[Country] and
      (__ \ "postcode").readNullable[String] and
      (__ \ "poBox").readNullable[String]
  )(MembersLookupLastUkAddress.apply _)

  implicit val writes: OWrites[MembersLookupLastUkAddress] = OWrites[MembersLookupLastUkAddress] { address =>
    Json.obj(
      "line1"    -> address.line1,
      "line2"    -> address.line2,
      "line3"    -> address.line3,
      "line4"    -> address.line4,
      "country"  -> address.country,
      "postcode" -> address.postcode
    )
  }

  implicit val format: OFormat[MembersLookupLastUkAddress] = OFormat(reads, writes)

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
  val line1: String          = addressLine1
  val line2: String          = addressLine2
  val line3: Option[String]  = addressLine3
  val line4: Option[String]  = addressLine4
  val line5: Option[Nothing] = None
}

object MembersCurrentAddress {

  implicit val reads: Reads[MembersCurrentAddress] = (
    (__ \ "line1").read[String] and
      (__ \ "line2").read[String] and
      (__ \ "line3").readNullable[String] and
      (__ \ "line4").readNullable[String] and
      (__ \ "country").read[Country] and
      (__ \ "postcode").readNullable[String] and
      (__ \ "poBox").readNullable[String]
  )(MembersCurrentAddress.apply _)

  implicit val writes: OWrites[MembersCurrentAddress] = OWrites[MembersCurrentAddress] { address =>
    Json.obj(
      "line1"    -> address.line1,
      "line2"    -> address.line2,
      "line3"    -> address.line3,
      "line4"    -> address.line4,
      "country"  -> address.country,
      "postcode" -> address.postcode,
      "poBox"    -> address.poBox
    )
  }

  implicit val format: OFormat[MembersCurrentAddress] = OFormat(reads, writes)

  def fromAddress(address: Address): MembersCurrentAddress = {
    MembersCurrentAddress(
      addressLine1 = address.line1,
      addressLine2 = address.line2,
      addressLine3 = address.line3,
      addressLine4 = address.line4,
      country      = address.country,
      postcode     = address.postcode,
      poBox        = address.poBox
    )
  }
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

  implicit val reads: Reads[QROPSAddress] = (
    (__ \ "line1").read[String] and
      (__ \ "line2").read[String] and
      (__ \ "line3").readNullable[String] and
      (__ \ "line4").readNullable[String] and
      (__ \ "line5").readNullable[String] and
      (__ \ "country").read[Country]
  )(QROPSAddress.apply _)

  implicit val writes: OWrites[QROPSAddress] = OWrites[QROPSAddress] { address =>
    Json.obj(
      "line1"   -> address.line1,
      "line2"   -> address.line2,
      "line3"   -> address.line3,
      "line4"   -> address.line4,
      "line5"   -> address.line5,
      "country" -> address.country
    )
  }

  implicit val format: OFormat[QROPSAddress] = OFormat(reads, writes)

  def fromAddress(address: Address): QROPSAddress = {
    QROPSAddress(
      addressLine1 = address.line1,
      addressLine2 = address.line2,
      addressLine3 = address.line3,
      addressLine4 = address.line4,
      addressLine5 = address.line5,
      country      = address.country
    )
  }
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
  val countryCode: Country              = country
  val poBox: Option[String]             = None
  override val postcode: Option[String] = None
}

object SchemeManagersAddress {

  implicit val reads: Reads[SchemeManagersAddress] = (
    (__ \ "line1").read[String] and
      (__ \ "line2").read[String] and
      (__ \ "line3").readNullable[String] and
      (__ \ "line4").readNullable[String] and
      (__ \ "line5").readNullable[String] and
      (__ \ "country").read[Country]
  )(SchemeManagersAddress.apply _)

  implicit val writes: OWrites[SchemeManagersAddress] = OWrites[SchemeManagersAddress] { address =>
    Json.obj(
      "line1"   -> address.line1,
      "line2"   -> address.line2,
      "line3"   -> address.line3,
      "line4"   -> address.line4,
      "line5"   -> address.addressLine5,
      "country" -> address.country
    )
  }

  implicit val format: OFormat[SchemeManagersAddress] = OFormat(reads, writes)

  def fromAddress(address: Address): SchemeManagersAddress = {
    SchemeManagersAddress(
      addressLine1 = address.line1,
      addressLine2 = address.line2,
      addressLine3 = address.line3,
      addressLine4 = address.line4,
      addressLine5 = None,
      country      = address.country
    )
  }
}

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

sealed trait Address {
  val line1: String
  val line2: Option[String]
  val line3: Option[String]
  val line4: Option[String]
  val townOrCity: Option[String]
  /*TODO
     Once we implement the country look up on the manual entry page, the country should be changed to the country object itself
     which contains a country code and country name (see the country model)
   */
  val county: Option[String]
  val country: Option[String]
  val postcode: Option[String]
}

object Address {

  implicit val writes: OWrites[Address] = OWrites[Address] { address =>
    val base = Json.obj(
      "line1"      -> address.line1,
      "line2"      -> address.line2,
      "line3"      -> address.line3,
      "line4"      -> address.line4,
      "townOrCity" -> address.townOrCity,
      "county"     -> address.county,
      "country"    -> address.country,
      "postcode"   -> address.postcode
    )

    val withType = address match {
      case _: MembersLastUKAddress       => base + ("type" -> JsString("MembersLastUKAddress"))
      case _: MembersLookupLastUkAddress => base + ("type" -> JsString("MembersLookupLastUkAddress"))
      case _: MembersCurrentAddress      => base + ("type" -> JsString("MembersCurrentAddress"))
    }
    withType
  }

  implicit val reads: Reads[Address] =
    (__ \ "type").read[String].flatMap {
      case "MembersLastUKAddress" =>
        for {
          line1      <- (__ \ "line1").read[String]
          line2      <- (__ \ "line2").readNullable[String]
          townOrCity <- (__ \ "townOrCity").readNullable[String]
          county     <- (__ \ "county").readNullable[String]
          postcode   <- (__ \ "postcode").readNullable[String]
        } yield {
          MembersLastUKAddress(
            addressLine1  = line1,
            addressLine2  = line2,
            rawTownOrCity = townOrCity.getOrElse(""),
            county        = county,
            rawPostcode   = postcode.getOrElse("")
          )
        }

      case "MembersLookupLastUkAddress" =>
        for {
          line1      <- (__ \ "line1").read[String]
          line2      <- (__ \ "line2").readNullable[String]
          line3      <- (__ \ "line3").readNullable[String]
          line4      <- (__ \ "line4").readNullable[String]
          townOrCity <- (__ \ "townOrCity").readNullable[String]
          country    <- (__ \ "country").readNullable[String]
          postcode   <- (__ \ "postcode").readNullable[String]
        } yield {
          MembersLookupLastUkAddress(
            line1      = line1,
            line2      = line2,
            line3      = line3,
            line4      = line4,
            townOrCity = townOrCity,
            country    = country,
            postcode   = postcode
          )
        }

      case "MembersCurrentAddress" =>
        for {
          line1      <- (__ \ "line1").read[String]
          line2      <- (__ \ "line2").read[String]
          line3      <- (__ \ "line3").readNullable[String]
          townOrCity <- (__ \ "townOrCity").readNullable[String]
          country    <- (__ \ "country").readNullable[String]
          postcode   <- (__ \ "postcode").readNullable[String]
        } yield {
          MembersCurrentAddress(
            addressLine1 = line1,
            addressLine2 = line2,
            addressLine3 = line3,
            townOrCity   = townOrCity,
            country      = country,
            postcode     = postcode
          )
        }

      case other =>
        Reads(_ => JsError(s"Unknown Address type: $other"))
    }

  implicit val format: OFormat[Address] = OFormat(reads, writes)
}

case class MembersLastUKAddress(
    addressLine1: String,
    addressLine2: Option[String],
    rawTownOrCity: String,
    county: Option[String],
    rawPostcode: String
  ) extends Address {
  val line1: String              = addressLine1
  val line2: Option[String]      = addressLine2
  val line3: Option[String]      = None
  val line4: Option[String]      = None
  val country: Option[String]    = None
  val townOrCity: Option[String] = Some(rawTownOrCity)
  val postcode: Option[String]   = Some(rawPostcode)
}

object MembersLastUKAddress {

  def fromAddress(address: Address): MembersLastUKAddress = {
    MembersLastUKAddress(
      addressLine1  = address.line1,
      addressLine2  = address.line2,
      rawTownOrCity = address.townOrCity.getOrElse(""),
      county        = address.county,
      rawPostcode   = address.postcode.getOrElse("")
    )
  }
}

case class MembersLookupLastUkAddress(
    line1: String,
    line2: Option[String],
    line3: Option[String],
    line4: Option[String],
    townOrCity: Option[String],
    country: Option[String],
    postcode: Option[String]
  ) extends Address {
  val county: Option[Nothing] = None
}

object MembersLookupLastUkAddress {

  def fromRawAddress(rawAddress: RawAddress): MembersLookupLastUkAddress = {
    MembersLookupLastUkAddress(
      line1      = rawAddress.lines.headOption.getOrElse(""),
      line2      = rawAddress.lines.lift(1),
      line3      = rawAddress.lines.lift(2),
      line4      = rawAddress.lines.lift(3),
      townOrCity = Some(rawAddress.town),
      postcode   = Some(rawAddress.postcode),
      country    = Some(rawAddress.country.name)
    )
  }
}

case class MembersCurrentAddress(
    addressLine1: String,
    addressLine2: String,
    addressLine3: Option[String],
    townOrCity: Option[String],
    country: Option[String],
    postcode: Option[String]
  ) extends Address {
  val line1: String          = addressLine1
  val line2: Option[String]  = Some(addressLine2)
  val line3: Option[String]  = addressLine3
  val line4: Option[String]  = None
  val county: Option[String] = None
}

object MembersCurrentAddress {

  def fromAddress(address: Address): MembersCurrentAddress = {
    MembersCurrentAddress(
      addressLine1 = address.line1,
      addressLine2 = address.line2.getOrElse(""),
      addressLine3 = address.line3,
      townOrCity   = address.townOrCity,
      country      = address.country,
      postcode     = address.postcode
    )
  }
}

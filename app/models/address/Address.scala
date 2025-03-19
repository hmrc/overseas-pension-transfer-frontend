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

import play.api.libs.json.{JsObject, JsResult, JsValue, Json, OFormat}

sealed trait Address {
  val line1: String
  val line2: Option[String]
  val line3: Option[String]
  val townOrCity: Option[String]
  /*TODO
     Once we implement the country look up on the manual entry page, the country should be changed to the country object itself
     which contains a country code and country name (see the country model)
   */
  val country: Option[String]
  val postcode: Option[String]
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
  val country: Option[String]    = None
  val townOrCity: Option[String] = Some(rawTownOrCity)
  val postcode: Option[String]   = Some(rawPostcode)
}

object MembersLastUKAddress {

  implicit val format: OFormat[MembersLastUKAddress] = new OFormat[MembersLastUKAddress] {

    override def reads(json: JsValue): JsResult[MembersLastUKAddress] = {
      for {
        addressLine1 <- (json \ "addressLine1").validate[String]
        addressLine2 <- (json \ "addressLine2").validateOpt[String]
        townOrCity   <- (json \ "townOrCity").validate[String]
        county       <- (json \ "county").validateOpt[String]
        postcode     <- (json \ "postcode").validate[String]
      } yield MembersLastUKAddress(
        addressLine1  = addressLine1,
        addressLine2  = addressLine2,
        rawTownOrCity = townOrCity,
        county        = county,
        rawPostcode   = postcode
      )
    }

    override def writes(address: MembersLastUKAddress): JsObject = Json.obj(
      "addressLine1" -> address.addressLine1,
      "addressLine2" -> address.addressLine2,
      "townOrCity"   -> address.rawTownOrCity,
      "county"       -> address.county,
      "postcode"     -> address.rawPostcode
    )
  }

  def fromLookupAddress(address: MembersLookupLastUkAddress): MembersLastUKAddress = {
    MembersLastUKAddress(
      addressLine1  = address.line1,
      addressLine2  = address.line2,
      rawTownOrCity = address.townOrCity.getOrElse(""),
      county        = None,
      rawPostcode   = address.postcode.getOrElse("")
    )
  }
}

case class MembersLookupLastUkAddress(
    line1: String,
    line2: Option[String],
    line3: Option[String],
    townOrCity: Option[String],
    country: Option[String],
    postcode: Option[String]
  ) extends Address

object MembersLookupLastUkAddress {

  def fromRawAddress(rawAddress: RawAddress): MembersLookupLastUkAddress = {
    MembersLookupLastUkAddress(
      line1      = rawAddress.lines.headOption.getOrElse(""),
      line2      = rawAddress.lines.lift(1),
      line3      = rawAddress.lines.lift(2),
      townOrCity = Some(rawAddress.town),
      postcode   = Some(rawAddress.postcode),
      country    = Some(rawAddress.country.name)
    )
  }

  implicit val format: OFormat[MembersLookupLastUkAddress] = Json.format

}

case class MembersCurrentAddress(
    addressLine1: String,
    addressLine2: String,
    addressLine3: Option[String],
    townOrCity: Option[String],
    country: Option[String],
    postcode: Option[String]
  ) extends Address {
  val line1: String         = addressLine1
  val line2: Option[String] = Some(addressLine2)
  val line3: Option[String] = addressLine3
}

object MembersCurrentAddress {

  implicit val format: OFormat[MembersCurrentAddress] = Json.format
}

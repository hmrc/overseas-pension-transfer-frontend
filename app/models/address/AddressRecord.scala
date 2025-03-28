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

import play.api.libs.json.{JsValue, Json, OFormat}

case class LocalCustodian(code: Int, name: String)

object LocalCustodian {
  implicit val format: OFormat[LocalCustodian] = Json.format
}

case class Subdivision(code: String, name: String)

object Subdivision {
  implicit val format: OFormat[Subdivision] = Json.format[Subdivision]
}

case class RawAddress(lines: List[String], town: String, postcode: String, subdivision: Option[Subdivision], country: Country)

object RawAddress {
  implicit val format: OFormat[RawAddress] = Json.format
}

case class AddressRecord(
    id: String,
    uprn: Option[Long],
    parentUprn: Option[Long],
    usrn: Option[Long],
    organisation: Option[String],
    address: RawAddress,
    // ISO639-1 code, e.g. 'en' for English
    // see https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes
    language: String,
    localCustodian: Option[LocalCustodian],
    location: Option[Seq[BigDecimal]],
    blpuState: Option[String],
    logicalState: Option[String],
    streetClassification: Option[String],
    administrativeArea: Option[String] = None,
    poBox: Option[String]              = None
  )

object AddressRecord {
  implicit val format: OFormat[AddressRecord] = Json.format
}

case class RecordSet(addresses: Seq[AddressRecord])

object RecordSet {

  def apply(addressListAsJson: JsValue): RecordSet = {
    val addresses = addressListAsJson.as[Seq[AddressRecord]]
    RecordSet(addresses)
  }

  implicit val format: OFormat[RecordSet] = Json.format
}

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

sealed trait AddressLookupResult
case class AddressRecords(postcode: String, records: Seq[AddressRecord]) extends AddressLookupResult
case class NoAddressFound(postcode: String)                              extends AddressLookupResult

object AddressLookupResult {

  implicit val addressRecordsFormat: OFormat[AddressRecords] = Json.format[AddressRecords]
  implicit val noAddressFoundFormat: OFormat[NoAddressFound] = Json.format[NoAddressFound]

  implicit val format: Format[AddressLookupResult] = new Format[AddressLookupResult] {

    override def reads(json: JsValue): JsResult[AddressLookupResult] = {
      val postcode = (json \ "postcode").validate[String]
      val records  = (json \ "records").validateOpt[Seq[AddressRecord]]

      (postcode, records) match {
        case (JsSuccess(pc, _), JsSuccess(Some(rs), _)) if rs.nonEmpty => JsSuccess(AddressRecords(pc, rs))
        case (JsSuccess(pc, _), _)                                     => JsSuccess(NoAddressFound(pc))
        case _                                                         => JsError("Invalid AddressLookupResult")
      }
    }

    override def writes(result: AddressLookupResult): JsValue = result match {
      case AddressRecords(pc, rs) => Json.obj("postcode" -> pc, "records" -> rs)
      case NoAddressFound(pc)     => Json.obj("postcode" -> pc)
    }
  }
}

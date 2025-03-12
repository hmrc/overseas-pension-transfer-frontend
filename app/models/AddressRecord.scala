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

import play.api.libs.json.{JsString, JsValue, Json, OFormat, Writes}

case class AddressRecord(
    id: String,
    address: UkAddress
  )

object AddressRecord {
  implicit val format: OFormat[AddressRecord] = Json.format
}

case class RecordSet(addresses: Seq[AddressRecord])

object RecordSet {

  def fromJsonAddressLookupService(addressListAsJson: JsValue): RecordSet = {
    val addresses = addressListAsJson.as[Seq[AddressRecord]]
    RecordSet(addresses)
  }

  implicit val format: OFormat[RecordSet] = Json.format
}

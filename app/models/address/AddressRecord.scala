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

case class RawAddress(lines: List[String], town: String, postcode: String, country: Country)

object RawAddress {
  implicit val format: OFormat[RawAddress] = Json.format
}

case class AddressRecord(
    id: String,
    address: RawAddress,
    poBox: Option[String] = None
  )

object AddressRecord {
  implicit val format: OFormat[AddressRecord] = Json.format
}

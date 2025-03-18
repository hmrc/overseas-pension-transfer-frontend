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

case class UkAddress(line1: String, line2: String, line3: Option[String], city: Option[String], country: Option[String], postcode: Option[String])
    extends Address

object UkAddress {

  def fromRawAddress(rawAddress: RawAddress): UkAddress = {
    UkAddress(
      line1    = if (rawAddress.lines.nonEmpty) rawAddress.lines.head else "",
      line2    = if (rawAddress.lines.size > 1) rawAddress.lines(1) else "",
      line3    = rawAddress.lines.lift(2),
      city     = Some(rawAddress.town),
      postcode = Some(rawAddress.postcode),
      country  = Some(rawAddress.country.name)
    )
  }

  implicit val format: OFormat[UkAddress] = Json.format

}

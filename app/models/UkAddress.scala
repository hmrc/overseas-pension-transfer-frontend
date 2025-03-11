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

case class UkAddress(lines: List[String], town: String, rawPostCode: String, rawCountry: Country) extends Address {

  val addressLine1: String = if (lines.nonEmpty) lines.head else ""

  val addressLine2: String = if (lines.size > 1) lines(1) else ""

  val addressLine3: Option[String] = lines.lift(2)

  val city: Option[String] = Some(town)

  val postcode: Option[String] = Some(rawPostCode)

  val country: Option[String] = Some(rawCountry.name)
}

object UkAddress {

  implicit val format: OFormat[UkAddress] = (
    (__ \ "lines").format[List[String]] and
      (__ \ "town").format[String] and
      (__ \ "postcode").format[String] and
      (__ \ "country").format[Country]
  )(UkAddress.apply, unlift(UkAddress.unapply))

}

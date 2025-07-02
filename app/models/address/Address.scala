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
import play.api.libs.functional.syntax._

trait Address {
  def addressType: AddressType
  def base: BaseAddress
}

object Address {

  val applyTypes: Map[AddressType, BaseAddress => Address] = Map(
    AddressType.MembersLastUK       -> MembersLastUKAddress.apply,
    AddressType.MembersLookupLastUK -> MembersLookupLastUKAddress.apply,
    AddressType.MembersCurrent      -> MembersCurrentAddress.apply,
    AddressType.QROPS               -> QROPSAddress.apply,
    AddressType.SchemeManager       -> SchemeManagersAddress.apply,
    AddressType.Property            -> PropertyAddress.apply
  )

  implicit val reads: Reads[Address] = (
    (__ \ "type").read[AddressType] and
      __.read[BaseAddress]
  ).tupled.flatMap {
    case (typ, base) =>
      applyTypes.get(typ)
        .map(f => Reads.pure(f(base)))
        .getOrElse(Reads(_ => JsError("Unknown address type")))
  }

  implicit val writes: OWrites[Address] = OWrites(a =>
    BaseAddress.writes.writes(a.base).as[JsObject] + ("type" -> Json.toJson(a.addressType))
  )

  implicit val format: OFormat[Address] = OFormat(reads, writes)
}

case class BaseAddress(
    line1: String,
    line2: String,
    line3: Option[String]    = None,
    line4: Option[String]    = None,
    line5: Option[String]    = None,
    country: Country,
    postcode: Option[String] = None,
    poBox: Option[String]    = None
  )

object BaseAddress {

  implicit val reads: Reads[BaseAddress] = (
    (__ \ "line1").read[String] and
      (__ \ "line2").read[String] and
      (__ \ "line3").readNullable[String] and
      (__ \ "line4").readNullable[String] and
      (__ \ "line5").readNullable[String] and
      (__ \ "country").read[Country] and
      (__ \ "postcode").readNullable[String] and
      (__ \ "poBox").readNullable[String]
  )(BaseAddress.apply _)

  implicit val writes: OWrites[BaseAddress] = OWrites { a =>
    Json.obj(
      "line1"    -> a.line1,
      "line2"    -> a.line2,
      "line3"    -> a.line3,
      "line4"    -> a.line4,
      "line5"    -> a.line5,
      "country"  -> a.country,
      "postcode" -> a.postcode,
      "poBox"    -> a.poBox
    ).fields.filterNot(_._2 == JsNull).foldLeft(Json.obj())(_ + _)
  }
}

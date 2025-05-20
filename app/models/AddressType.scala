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

import play.api.libs.json._
sealed trait AddressType

object AddressType {
  case object MembersCurrentAddress      extends AddressType
  case object MembersLastUKAddress       extends AddressType
  case object MembersLookupLastUkAddress extends AddressType
  case object QROPSAddress               extends AddressType

  val values = Seq(MembersCurrentAddress, MembersLookupLastUkAddress)

  implicit val format: Format[AddressType] = new Format[AddressType] {

    def reads(json: JsValue): JsResult[AddressType] = json match {
      case JsString("MembersCurrentAddress")      => JsSuccess(MembersCurrentAddress)
      case JsString("MembersLastUKAddress")       => JsSuccess(MembersLookupLastUkAddress)
      case JsString("MembersLookupLastUkAddress") => JsSuccess(MembersLookupLastUkAddress)
      case JsString("QROPSAddress")               => JsSuccess(MembersLookupLastUkAddress)
      case _                                      => JsError("Unknown Address type")
    }
    def writes(a: AddressType): JsValue             = JsString(a.toString)
  }
}

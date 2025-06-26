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

case class ShareEntry(
    companyName: String,
    valueOfShares: BigDecimal,
    numberOfShares: String,
    classOfShares: String,
    shareType: ShareType
  )

object ShareEntry {
  implicit val format: OFormat[ShareEntry] = Json.format[ShareEntry]
}

sealed trait ShareType {

  override def toString: String = this match {
    case ShareType.Quoted   => TypeOfAsset.QuotedShares.toString
    case ShareType.Unquoted => TypeOfAsset.UnquotedShares.toString
  }
}

object ShareType {
  case object Quoted   extends ShareType
  case object Unquoted extends ShareType

  implicit val reads: Reads[ShareType] = Reads {
    case JsString(TypeOfAsset.QuotedShares.toString)   => JsSuccess(Quoted)
    case JsString(TypeOfAsset.UnquotedShares.toString) => JsSuccess(Unquoted)
  }

  implicit val writes: Writes[ShareType] = Writes {
    case Quoted   => JsString(TypeOfAsset.QuotedShares.toString)
    case Unquoted => JsString(TypeOfAsset.UnquotedShares.toString)
  }

  implicit val format: Format[ShareType] = Format(reads, writes)
}

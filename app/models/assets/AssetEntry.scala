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

package models.assets

import models.address.PropertyAddress
import play.api.libs.json._

sealed trait AssetEntry

case class QuotedSharesEntry(
    companyName: String,
    valueOfShares: BigDecimal,
    numberOfShares: String,
    classOfShares: String
  ) extends AssetEntry

object QuotedSharesEntry {
  val CompanyName    = "companyName"
  val ValueOfShares  = "valueOfShares"
  val NumberOfShares = "numberOfShares"
  val ClassOfShares  = "classOfShares"

  implicit val format: OFormat[QuotedSharesEntry] = Json.format[QuotedSharesEntry]
}

case class UnquotedSharesEntry(
    companyName: String,
    valueOfShares: BigDecimal,
    numberOfShares: String,
    classOfShares: String
  ) extends AssetEntry

object UnquotedSharesEntry {
  val CompanyName    = "companyName"
  val ValueOfShares  = "valueOfShares"
  val NumberOfShares = "numberOfShares"
  val ClassOfShares  = "classOfShares"

  implicit val format: OFormat[UnquotedSharesEntry] = Json.format[UnquotedSharesEntry]
}

case class PropertyEntry(
    propertyAddress: PropertyAddress,
    propValue: BigDecimal,
    propDescription: String
  ) extends AssetEntry

object PropertyEntry {
  val PropertyAddress = "propertyAddress"
  val PropValue       = "propValue"
  val PropDescription = "propDescription"

  implicit val format: OFormat[PropertyEntry] = Json.format[PropertyEntry]
}

case class OtherAssetsEntry(
    otherAssetsValueDescription: String,
    otherAssetsValue: BigDecimal
  ) extends AssetEntry

object OtherAssetsEntry {
  val OtherAssetsValueDescription = "valueDescription"
  val OtherAssetsValue            = "valueOfAsset"

  implicit val format: OFormat[OtherAssetsEntry] = Json.format[OtherAssetsEntry]
}

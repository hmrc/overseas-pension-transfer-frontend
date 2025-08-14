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
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json._

sealed trait AssetEntry

case class QuotedSharesEntry(
    companyName: String,
    valueOfShares: BigDecimal,
    numberOfShares: String,
    classOfShares: String
  ) extends AssetEntry

object QuotedSharesEntry {
  val CompanyName    = "quotedCompany"
  val ValueOfShares  = "quotedValue"
  val NumberOfShares = "quotedShareTotal"
  val ClassOfShares  = "quotedClass"

  val reads: Reads[QuotedSharesEntry] = (
    (__ \ CompanyName).read[String] and
      (__ \ ValueOfShares).read[BigDecimal] and
      (__ \ NumberOfShares).read[String] and
      (__ \ ClassOfShares).read[String]
  )(QuotedSharesEntry.apply _)

  val writes: OWrites[QuotedSharesEntry] = (
    (__ \ CompanyName).write[String] and
      (__ \ ValueOfShares).write[BigDecimal] and
      (__ \ NumberOfShares).write[String] and
      (__ \ ClassOfShares).write[String]
  )(unlift(QuotedSharesEntry.unapply))

  implicit val format: OFormat[QuotedSharesEntry] = OFormat(reads, writes)
}

case class UnquotedSharesEntry(
    companyName: String,
    valueOfShares: BigDecimal,
    numberOfShares: String,
    classOfShares: String
  ) extends AssetEntry

object UnquotedSharesEntry {
  val CompanyName    = "unquotedCompany"
  val ValueOfShares  = "unquotedValue"
  val NumberOfShares = "unquotedShareTotal"
  val ClassOfShares  = "unquotedClass"

  val reads: Reads[UnquotedSharesEntry] = (
    (__ \ CompanyName).read[String] and
      (__ \ ValueOfShares).read[BigDecimal] and
      (__ \ NumberOfShares).read[String] and
      (__ \ ClassOfShares).read[String]
  )(UnquotedSharesEntry.apply _)

  val writes: OWrites[UnquotedSharesEntry] = (
    (__ \ CompanyName).write[String] and
      (__ \ ValueOfShares).write[BigDecimal] and
      (__ \ NumberOfShares).write[String] and
      (__ \ ClassOfShares).write[String]
  )(unlift(UnquotedSharesEntry.unapply))

  implicit val format: OFormat[UnquotedSharesEntry] = OFormat(reads, writes)
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

  val reads: Reads[PropertyEntry] = (
    (__ \ PropertyAddress).read[PropertyAddress] and
      (__ \ PropValue).read[BigDecimal] and
      (__ \ PropDescription).read[String]
  )(PropertyEntry.apply _)

  val writes: OWrites[PropertyEntry] = (
    (__ \ PropertyAddress).write[PropertyAddress] and
      (__ \ PropValue).write[BigDecimal] and
      (__ \ PropDescription).write[String]
  )(unlift(PropertyEntry.unapply))

  implicit val format: OFormat[PropertyEntry] = OFormat(reads, writes)
}

case class OtherAssetsEntry(
    assetDescription: String,
    assetValue: BigDecimal
  ) extends AssetEntry

object OtherAssetsEntry {
  val AssetDescription = "assetDescription"
  val AssetValue       = "assetValue"

  val reads: Reads[OtherAssetsEntry] = (
    (__ \ AssetDescription).read[String] and
      (__ \ AssetValue).read[BigDecimal]
  )(OtherAssetsEntry.apply _)

  val writes: OWrites[OtherAssetsEntry] = (
    (__ \ AssetDescription).write[String] and
      (__ \ AssetValue).write[BigDecimal]
  )(unlift(OtherAssetsEntry.unapply))

  implicit val format: OFormat[OtherAssetsEntry] = OFormat(reads, writes)
}

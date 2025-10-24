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

package models.transferJourneys

import models.address.PropertyAddress
import models.assets.TypeOfAsset
import models.{ApplicableTaxExclusions, WhyTransferIsNotTaxable, WhyTransferIsTaxable}
import java.time.LocalDate

case class TransferDetails(
    allowanceBeforeTransfer: BigDecimal,
    transferAmount: BigDecimal,
    isTransferTaxable: Boolean,
    whyTaxable: WhyTransferIsTaxable,
    whyNotTaxable: Set[WhyTransferIsNotTaxable],
    applicableTaxExclusions: Set[ApplicableTaxExclusions],
    amountOfTaxDeducted: BigDecimal,
    netTransferAmount: BigDecimal,
    dateOfTransfer: LocalDate,
    isTransferCashOnly: Boolean,
    typeOfAsset: Seq[TypeOfAsset],
    cashAmountInTransfer: Option[BigDecimal],
    otherAssets: List[OtherAssetsDetails]         = Nil,
    propertyDetails: Option[PropertyDetails]      = None,
    unquotedShares: Option[UnquotedSharesDetails] = None,
    quotedShares: Option[QuotedSharesDetails]     = None
  )

case class UnquotedSharesDetails(
    unquotedCompanyName: String,
    unquotedShareValue: BigDecimal,
    unquotedNumberOfShares: Int,
    unquotedShareClass: String
  )

case class QuotedSharesDetails(
    quotedCompanyName: String,
    quotedShareValue: BigDecimal,
    quotedNumberOfShares: Int,
    quotedShareClass: String
  )

case class PropertyDetails(
    propertyAddress: PropertyAddress,
    propertyValue: BigDecimal,
    propertyDescription: String
  )

case class OtherAssetsDetails(
    otherAssetsDescription: String,
    otherAssetsValue: BigDecimal
  )

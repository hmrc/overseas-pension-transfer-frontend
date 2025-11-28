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

package validators

import base.SpecBase
import cats.data.Chain
import cats.data.Validated.{Invalid, Valid}
import models.ApplicableTaxExclusions.Occupational
import models.WhyTransferIsNotTaxable.IndividualIsEmployeeOccupational
import models.WhyTransferIsTaxable.{NoExclusion, TransferExceedsOTCAllowance}
import models.address.{Country, PropertyAddress}
import models.assets.TypeOfAsset.{Cash, Other}
import models.assets._
import models.transferJourneys._
import models.{ApplicableTaxExclusions, DataMissingError, GenericError, WhyTransferIsTaxable}
import org.scalatest.freespec.AnyFreeSpec
import pages.transferDetails._
import pages.transferDetails.assetsMiniJourneys.cash.CashAmountInTransferPage
import pages.transferDetails.assetsMiniJourneys.otherAssets.{MoreOtherAssetsDeclarationPage, OtherAssetsDescriptionPage, OtherAssetsValuePage}
import pages.transferDetails.assetsMiniJourneys.property.{MorePropertyDeclarationPage, PropertyAddressPage, PropertyDescriptionPage, PropertyValuePage}
import pages.transferDetails.assetsMiniJourneys.quotedShares._
import pages.transferDetails.assetsMiniJourneys.unquotedShares._
import play.api.libs.json.Json
import queries.assets.{OtherAssetsQuery, PropertyQuery, QuotedSharesQuery, UnquotedSharesQuery}

import java.time.LocalDate

class TransferDetailsValidatorSpec extends AnyFreeSpec with SpecBase {

  private val testCashAmount         = BigDecimal(500.02)
  private val testUnquotedShareValue = BigDecimal(1000.09)
  private val testUnquotedShareCount = 103
  private val testQuotedShareValue   = BigDecimal(2025.25)
  private val testQuotedShareCount   = 200
  private val testAssetValue1        = BigDecimal(8999.99)
  private val testAssetValue2        = BigDecimal(12345.55)
  private val testCompanyName1       = "Bankers"
  private val testCompanyName2       = "Traders"
  private val testShareClass1        = "First class"
  private val testShareClass2        = "Second class"
  private val testAssetDesc1         = "Crypto"
  private val testAssetDesc2         = "Bullion"

  private val testPropertyAddress = PropertyAddress(
    addressLine1 = "Line 1",
    addressLine2 = "Line 2",
    addressLine3 = None,
    addressLine4 = None,
    country      = Country("GB", "United Kingdom"),
    ukPostCode   = None
  )

  private val validator = TransferDetailsValidator
  private val today     = LocalDate.now

  private val baseTransferDetails = TransferDetails(
    allowanceBeforeTransfer = 1000.25,
    transferAmount          = 2000.88,
    isTransferTaxable       = true,
    whyTaxable              = Some(WhyTransferIsTaxable.TransferExceedsOTCAllowance),
    whyNotTaxable           = None,
    applicableTaxExclusions = None,
    amountOfTaxDeducted     = Some(100.33),
    netTransferAmount       = Some(1900.99),
    dateOfTransfer          = today,
    isTransferCashOnly      = true,
    typeOfAsset             = Seq(Cash),
    cashAmountInTransfer    = None,
    unquotedShares          = None,
    moreThan5Unquoted       = None,
    quotedShares            = None,
    moreThan5Quoted         = None,
    propertyDetails         = None,
    moreThan5Property       = None,
    otherAssets             = None,
    moreThan5OtherAssets    = None
  )

  private def buildBaseUserAnswers = emptyUserAnswers
    .set(OverseasTransferAllowancePage, BigDecimal(1000.25)).success.value
    .set(AmountOfTransferPage, BigDecimal(2000.88)).success.value
    .set(IsTransferTaxablePage, true).success.value
    .set(WhyTransferIsTaxablePage, WhyTransferIsTaxable.TransferExceedsOTCAllowance).success.value
    .set(ApplicableTaxExclusionsPage, Set[ApplicableTaxExclusions](ApplicableTaxExclusions.Occupational)).success.value
    .set(AmountOfTaxDeductedPage, BigDecimal(100.33)).success.value
    .set(NetTransferAmountPage, BigDecimal(1900.99)).success.value
    .set(DateOfTransferPage, today).success.value
    .set(IsTransferCashOnlyPage, true).success.value
    .set(TypeOfAssetPage, Seq(Cash)).success.value

  "fromUserAnswers" - {
    "must return valid TransferDetails" - {
      "when all required fields are provided" in {
        val userAnswers = buildBaseUserAnswers
          .set(CashAmountInTransferPage, BigDecimal(2000.88)).success.value

        validator.fromUserAnswers(userAnswers) mustBe
          Valid(baseTransferDetails.copy(
            applicableTaxExclusions = Some(Set(ApplicableTaxExclusions.Occupational)),
            cashAmountInTransfer    = Some(BigDecimal(2000.88))
          ))
      }

      "When transfer is taxable and WhyTransferIsTaxable is NoExclusion with ApplicableExclusion None" in {
        val validWithNoExclusion = Json.obj("transferDetails" -> Json.obj(
          "allowanceBeforeTransfer" -> 1000.25,
          "transferAmount"          -> 2000.88,
          "paymentTaxableOverseas"  -> true,
          "whyTaxableOT"            -> NoExclusion.toString,
          "amountTaxDeducted"       -> 100.33,
          "transferMinusTax"        -> 1900.99,
          "dateMemberTransferred"   -> today,
          "cashOnlyTransfer"        -> true,
          "cashValue"               -> 2000.88,
          "typeOfAsset"             -> List(Cash.toString)
        ))

        val userAnswers = emptyUserAnswers.copy(data = validWithNoExclusion)

        validator.fromUserAnswers(userAnswers) mustBe
          Valid(baseTransferDetails.copy(
            whyTaxable              = Some(NoExclusion),
            applicableTaxExclusions = None,
            isTransferCashOnly      = true,
            cashAmountInTransfer    = Some(BigDecimal(2000.88)),
            typeOfAsset             = Seq(Cash)
          ))
      }

      "when a transfer includes a cash asset" - {
        "when value is not cash only with a valid amount" in {
          val userAnswers = buildBaseUserAnswers
            .set(IsTransferCashOnlyPage, false).success.value
            .set(CashAmountInTransferPage, BigDecimal(2000.88)).success.value
            .set(TypeOfAssetPage, Seq(TypeOfAsset.Cash)).success.value
            .set(CashAmountInTransferPage, testCashAmount).success.value

          val expected = baseTransferDetails.copy(
            isTransferCashOnly      = false,
            typeOfAsset             = Seq(TypeOfAsset.Cash),
            cashAmountInTransfer    = Some(testCashAmount),
            applicableTaxExclusions = Some(Set(ApplicableTaxExclusions.Occupational))
          )

          validator.fromUserAnswers(userAnswers) mustBe Valid(expected)
        }
      }

      "when a transfer includes unquoted shares" - {
        "with valid share details" in {
          val userAnswers = buildBaseUserAnswers
            .set(IsTransferCashOnlyPage, false).success.value
            .set(TypeOfAssetPage, Seq(TypeOfAsset.UnquotedShares)).success.value
            .set(UnquotedSharesCompanyNamePage(0), testCompanyName1).success.value
            .set(UnquotedSharesValuePage(0), testUnquotedShareValue).success.value
            .set(UnquotedSharesNumberPage(0), testUnquotedShareCount).success.value
            .set(UnquotedSharesClassPage(0), testShareClass1).success.value

          val expected = baseTransferDetails.copy(
            typeOfAsset             = Seq(TypeOfAsset.UnquotedShares),
            unquotedShares          = Some(List(UnquotedSharesEntry(
              testCompanyName1,
              testUnquotedShareValue,
              testUnquotedShareCount,
              testShareClass1
            ))),
            applicableTaxExclusions = Some(Set(ApplicableTaxExclusions.Occupational)),
            isTransferCashOnly      = false
          )

          validator.fromUserAnswers(userAnswers) mustBe Valid(expected)
        }
      }

      "when a transfer includes property assets" - {
        "with valid property details" in {
          val userAnswers = buildBaseUserAnswers
            .set(IsTransferCashOnlyPage, false).success.value
            .set(TypeOfAssetPage, Seq(TypeOfAsset.Property)).success.value
            .set(PropertyAddressPage(0), testPropertyAddress).success.value
            .set(PropertyValuePage(0), BigDecimal(1000000.99)).success.value
            .set(PropertyDescriptionPage(0), "Bungalow").success.value

          val expected = baseTransferDetails.copy(
            typeOfAsset             = Seq(TypeOfAsset.Property),
            propertyDetails         = Some(List(PropertyEntry(
              testPropertyAddress,
              BigDecimal(1000000.99),
              "Bungalow"
            ))),
            applicableTaxExclusions = Some(Set(ApplicableTaxExclusions.Occupational)),
            isTransferCashOnly      = false
          )

          validator.fromUserAnswers(userAnswers) mustBe Valid(expected)
        }
      }

      "when a transfer includes quoted shares" - {
        "with valid share details" in {
          val userAnswers = buildBaseUserAnswers
            .set(IsTransferCashOnlyPage, false).success.value
            .set(TypeOfAssetPage, Seq(TypeOfAsset.QuotedShares)).success.value
            .set(QuotedSharesCompanyNamePage(0), testCompanyName2).success.value
            .set(QuotedSharesValuePage(0), testQuotedShareValue).success.value
            .set(QuotedSharesNumberPage(0), testQuotedShareCount).success.value
            .set(QuotedSharesClassPage(0), testShareClass2).success.value

          val expected = baseTransferDetails.copy(
            typeOfAsset             = Seq(TypeOfAsset.QuotedShares),
            quotedShares            = Some(List(QuotedSharesEntry(
              testCompanyName2,
              testQuotedShareValue,
              testQuotedShareCount,
              testShareClass2
            ))),
            applicableTaxExclusions = Some(Set(ApplicableTaxExclusions.Occupational)),
            isTransferCashOnly      = false
          )

          validator.fromUserAnswers(userAnswers) mustBe Valid(expected)
        }
      }

      "when a transfer includes other assets" - {
        "with valid asset details" in {
          val userAnswers = buildBaseUserAnswers
            .set(IsTransferCashOnlyPage, false).success.value
            .set(TypeOfAssetPage, Seq(TypeOfAsset.Other)).success.value
            .set(OtherAssetsDescriptionPage(0), testAssetDesc1).success.value
            .set(OtherAssetsValuePage(0), testAssetValue1).success.value
            .set(OtherAssetsDescriptionPage(1), testAssetDesc2).success.value
            .set(OtherAssetsValuePage(1), testAssetValue2).success.value

          val expected = baseTransferDetails.copy(
            typeOfAsset             = Seq(TypeOfAsset.Other),
            otherAssets             = Some(List(
              OtherAssetsEntry(
                testAssetDesc1,
                testAssetValue1
              ),
              OtherAssetsEntry(
                testAssetDesc2,
                testAssetValue2
              )
            )),
            applicableTaxExclusions = Some(Set(ApplicableTaxExclusions.Occupational)),
            isTransferCashOnly      = false
          )

          validator.fromUserAnswers(userAnswers) mustBe Valid(expected)
        }
      }

      "when a transfer has 5 added assets and more then 5 value" in {
        val otherAssetList = List(
          OtherAssetsEntry(testAssetDesc1, testAssetValue1),
          OtherAssetsEntry(testAssetDesc2, testAssetValue2),
          OtherAssetsEntry(testAssetDesc1, testAssetValue2),
          OtherAssetsEntry(testAssetDesc2, testAssetValue1),
          OtherAssetsEntry(testAssetDesc2, testAssetValue2)
        )

        val userAnswers = buildBaseUserAnswers
          .set(IsTransferCashOnlyPage, false).success.value
          .set(TypeOfAssetPage, Seq(TypeOfAsset.Other)).success.value
          .set(OtherAssetsQuery, otherAssetList).success.value
          .set(MoreOtherAssetsDeclarationPage, true).success.value

        val expected = baseTransferDetails.copy(
          typeOfAsset             = Seq(TypeOfAsset.Other),
          otherAssets             = Some(List(
            OtherAssetsEntry(testAssetDesc1, testAssetValue1),
            OtherAssetsEntry(testAssetDesc2, testAssetValue2),
            OtherAssetsEntry(testAssetDesc1, testAssetValue2),
            OtherAssetsEntry(testAssetDesc2, testAssetValue1),
            OtherAssetsEntry(testAssetDesc2, testAssetValue2)
          )),
          moreThan5OtherAssets    = Some(true),
          applicableTaxExclusions = Some(Set(ApplicableTaxExclusions.Occupational)),
          isTransferCashOnly      = false
        )

        validator.fromUserAnswers(userAnswers) mustBe Valid(expected)
      }
    }

    "must return errors" - {
      "when required fields are missing" in {
        val result = validator.fromUserAnswers(emptyUserAnswers)

        result mustBe Invalid(TransferDetailsValidator.notStarted)
      }

      "when a cash asset is selected but the amount is missing" in {
        val userAnswers = buildBaseUserAnswers
          .set(IsTransferCashOnlyPage, false).success.value
          .set(TypeOfAssetPage, Seq(TypeOfAsset.Cash)).success.value
          .remove(CashAmountInTransferPage).success.value

        val result = validator.fromUserAnswers(userAnswers)

        result mustBe Invalid(Chain(
          DataMissingError(CashAmountInTransferPage)
        ))
      }

      "when a transfer is paymentTaxableOverseas is true and WhyTransferTaxable is ExceedsOtcAllowance and applicableExclusions is missing" in {
        val validWithNoExclusion = Json.obj("transferDetails" -> Json.obj(
          "allowanceBeforeTransfer" -> 1000.25,
          "transferAmount"          -> 2000.88,
          "paymentTaxableOverseas"  -> true,
          "whyTaxableOT"            -> TransferExceedsOTCAllowance.toString,
          "amountTaxDeducted"       -> 100.33,
          "transferMinusTax"        -> 1900.99,
          "dateMemberTransferred"   -> today,
          "cashOnlyTransfer"        -> true,
          "cashValue"               -> 2000.88,
          "typeOfAsset"             -> List(Cash.toString)
        ))

        val userAnswers = emptyUserAnswers.copy(data = validWithNoExclusion)

        val result = validator.fromUserAnswers(userAnswers)

        result mustBe Invalid(Chain(
          DataMissingError(ApplicableTaxExclusionsPage)
        ))
      }

      "when a transfer is paymentTaxableOverseas is true and WhyTransferTaxable and applicableExclusions is missing" in {
        val validWithNoExclusion = Json.obj("transferDetails" -> Json.obj(
          "allowanceBeforeTransfer" -> 1000.25,
          "transferAmount"          -> 2000.88,
          "paymentTaxableOverseas"  -> true,
          "amountTaxDeducted"       -> 100.33,
          "transferMinusTax"        -> 1900.99,
          "dateMemberTransferred"   -> today,
          "cashOnlyTransfer"        -> true,
          "cashValue"               -> 2000.88,
          "typeOfAsset"             -> List(Cash.toString)
        ))

        val userAnswers = emptyUserAnswers.copy(data = validWithNoExclusion)

        val result = validator.fromUserAnswers(userAnswers)

        result mustBe Invalid(Chain(
          DataMissingError(WhyTransferIsTaxablePage),
          DataMissingError(ApplicableTaxExclusionsPage)
        ))
      }

      "when a transfer is paymentTaxableOverseas is false and WhyTransferIsNotTaxable is missing" in {
        val validWithNoExclusion = Json.obj("transferDetails" -> Json.obj(
          "allowanceBeforeTransfer" -> 1000.25,
          "transferAmount"          -> 2000.88,
          "paymentTaxableOverseas"  -> false,
          "amountTaxDeducted"       -> 100.33,
          "transferMinusTax"        -> 1900.99,
          "dateMemberTransferred"   -> today,
          "cashOnlyTransfer"        -> true,
          "cashValue"               -> 2000.88,
          "typeOfAsset"             -> List(Cash.toString)
        ))

        val userAnswers = emptyUserAnswers.copy(data = validWithNoExclusion)

        val result = validator.fromUserAnswers(userAnswers)

        result mustBe Invalid(Chain(
          DataMissingError(WhyTransferIsNotTaxablePage)
        ))
      }

      "when Type of asset and CashAmountInTransfer is missing" in {
        val userAnswers = buildBaseUserAnswers
          .set(IsTransferCashOnlyPage, false).success.value
          .remove(TypeOfAssetPage).success.value

        val result = validator.fromUserAnswers(userAnswers)

        result mustBe Invalid(Chain(
          DataMissingError(TypeOfAssetPage),
          DataMissingError(CashAmountInTransferPage),
          DataMissingError(UnquotedSharesQuery),
          DataMissingError(MoreUnquotedSharesDeclarationPage),
          DataMissingError(QuotedSharesQuery),
          DataMissingError(MoreQuotedSharesDeclarationPage),
          DataMissingError(PropertyQuery),
          DataMissingError(MorePropertyDeclarationPage),
          DataMissingError(OtherAssetsQuery),
          DataMissingError(MoreOtherAssetsDeclarationPage)
        ))
      }

      "when cash only is selected but the cash amount doesn't equal the transfer amount" in {
        val userAnswers = buildBaseUserAnswers
          .set(TypeOfAssetPage, Seq(TypeOfAsset.Cash)).success.value
          .set(IsTransferCashOnlyPage, true).success.value
          .set(CashAmountInTransferPage, BigDecimal(0.01)).success.value

        val result = validator.fromUserAnswers(userAnswers)

        result mustBe Invalid(Chain(
          GenericError("Cash amount must equal amount of transfer if transfer is cash only")
        ))
      }

      "when a property asset is selected but the details are missing" in {
        val userAnswers = buildBaseUserAnswers
          .set(IsTransferCashOnlyPage, false).success.value
          .set(TypeOfAssetPage, Seq(TypeOfAsset.Property)).success.value

        val result = validator.fromUserAnswers(userAnswers)

        result mustBe Invalid(Chain(
          DataMissingError(PropertyQuery)
        ))
      }

      "when unquoted shares are selected but the details are missing" in {
        val userAnswers = buildBaseUserAnswers
          .set(IsTransferCashOnlyPage, false).success.value
          .set(TypeOfAssetPage, Seq(TypeOfAsset.UnquotedShares)).success.value

        val result = validator.fromUserAnswers(userAnswers)

        result mustBe Invalid(Chain(
          DataMissingError(UnquotedSharesQuery)
        ))
      }

      "when quoted shares are selected but the details are missing" in {
        val userAnswers = buildBaseUserAnswers
          .set(IsTransferCashOnlyPage, false).success.value
          .set(TypeOfAssetPage, Seq(TypeOfAsset.QuotedShares)).success.value

        val result = validator.fromUserAnswers(userAnswers)

        result mustBe Invalid(Chain(
          DataMissingError(QuotedSharesQuery)
        ))
      }

      "when other assets are selected but the details are missing" in {
        val userAnswers = buildBaseUserAnswers
          .set(IsTransferCashOnlyPage, false).success.value
          .set(TypeOfAssetPage, Seq(TypeOfAsset.Other)).success.value

        val result = validator.fromUserAnswers(userAnswers)

        result mustBe Invalid(Chain(
          DataMissingError(OtherAssetsQuery)
        ))
      }

      "when paymentTaxableOverseas == true and WhyTransferNotTaxable is present" in {
        val validWithNoExclusion = Json.obj("transferDetails" -> Json.obj(
          "allowanceBeforeTransfer"  -> 1000.25,
          "transferAmount"           -> 2000.88,
          "paymentTaxableOverseas"   -> true,
          "whyTaxableOT"             -> NoExclusion.toString,
          "reasonNoOverseasTransfer" -> List(IndividualIsEmployeeOccupational.toString),
          "amountTaxDeducted"        -> 100.33,
          "transferMinusTax"         -> 1900.99,
          "dateMemberTransferred"    -> today,
          "cashOnlyTransfer"         -> true,
          "cashValue"                -> 2000.88,
          "typeOfAsset"              -> List(Cash.toString)
        ))

        val userAnswers = emptyUserAnswers.copy(data = validWithNoExclusion)

        validator.fromUserAnswers(userAnswers) mustBe Invalid(Chain(
          GenericError("reasonNoOverseasTransfer cannot be present when paymentTaxableOverseas is true")
        ))
      }

      "when paymentTaxableOverseas == false  and WhyTaxableOT and applicableExclusions are present" in {
        val validWithNoExclusion = Json.obj("transferDetails" -> Json.obj(
          "allowanceBeforeTransfer"  -> 1000.25,
          "transferAmount"           -> 2000.88,
          "paymentTaxableOverseas"   -> false,
          "whyTaxableOT"             -> TransferExceedsOTCAllowance.toString,
          "applicableExclusion"      -> List(Occupational.toString),
          "reasonNoOverseasTransfer" -> List(IndividualIsEmployeeOccupational.toString),
          "amountTaxDeducted"        -> 100.33,
          "transferMinusTax"         -> 1900.99,
          "dateMemberTransferred"    -> today,
          "cashOnlyTransfer"         -> true,
          "cashValue"                -> 2000.88,
          "typeOfAsset"              -> List(Cash.toString)
        ))

        val userAnswers = emptyUserAnswers.copy(data = validWithNoExclusion)

        validator.fromUserAnswers(userAnswers) mustBe Invalid(Chain(
          GenericError("whyTaxableOT cannot be present when paymentTaxableOverseas is false"),
          GenericError("applicableExclusion cannot be present when paymentTaxableOverseas is false")
        ))
      }

      "when cashValue exists when no Cash is present in TypeOfAsset" in {
        val validWithNoExclusion = Json.obj("transferDetails" -> Json.obj(
          "allowanceBeforeTransfer"  -> 1000.25,
          "transferAmount"           -> 2000.88,
          "paymentTaxableOverseas"   -> false,
          "reasonNoOverseasTransfer" -> List(IndividualIsEmployeeOccupational.toString),
          "amountTaxDeducted"        -> 100.33,
          "transferMinusTax"         -> 1900.99,
          "dateMemberTransferred"    -> today,
          "cashOnlyTransfer"         -> false,
          "cashValue"                -> 2000.88,
          "typeOfAsset"              -> List(Other.toString),
          "otherAssets"              -> List(
            Json.obj(
              "assetDescription" -> "Vintage Car",
              "assetValue"       -> 3000.44
            )
          )
        ))

        val userAnswers = emptyUserAnswers.copy(data = validWithNoExclusion)

        validator.fromUserAnswers(userAnswers) mustBe Invalid(Chain(
          GenericError("cashValue not expected when Cash is not present in type of assets")
        ))
      }
    }
  }
}

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
import models.assets.{OtherAssetsEntry, QuotedSharesEntry, TypeOfAsset, UnquotedSharesEntry}
import models.transferJourneys._
import models.{ApplicableTaxExclusions, DataMissingError, WhyTransferIsNotTaxable, WhyTransferIsTaxable}
import org.scalatest.freespec.AnyFreeSpec
import pages.transferDetails._
import pages.transferDetails.assetsMiniJourneys.cash.CashAmountInTransferPage
import pages.transferDetails.assetsMiniJourneys.otherAssets.{OtherAssetsDescriptionPage, OtherAssetsValuePage}
import pages.transferDetails.assetsMiniJourneys.property.{PropertyAddressPage, PropertyDescriptionPage, PropertyValuePage}
import pages.transferDetails.assetsMiniJourneys.quotedShares.{QuotedSharesClassPage, QuotedSharesCompanyNamePage, QuotedSharesNumberPage, QuotedSharesValuePage}
import pages.transferDetails.assetsMiniJourneys.unquotedShares.{
  MoreUnquotedSharesDeclarationPage,
  UnquotedSharesClassPage,
  UnquotedSharesCompanyNamePage,
  UnquotedSharesNumberPage,
  UnquotedSharesValuePage
}
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

  private val validator = TransferDetailsValidator
  private val today     = LocalDate.now

  private val baseTransferDetails = TransferDetails(
    allowanceBeforeTransfer = 1000.25,
    transferAmount          = 2000.88,
    isTransferTaxable       = true,
    whyTaxable              = WhyTransferIsTaxable.TransferExceedsOTCAllowance,
    whyNotTaxable           = Set.empty[WhyTransferIsNotTaxable],
    applicableTaxExclusions = Set.empty[ApplicableTaxExclusions],
    amountOfTaxDeducted     = 100.33,
    netTransferAmount       = 1900.99,
    dateOfTransfer          = today,
    isTransferCashOnly      = false,
    typeOfAsset             = Seq.empty,
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
    .set(IsTransferCashOnlyPage, false).success.value

  "fromUserAnswers" - {
    "must return valid TransferDetails" - {
      "when all required fields are provided" in {
        val userAnswers = buildBaseUserAnswers
          .set(TypeOfAssetPage, Seq.empty).success.value

        validator.fromUserAnswers(userAnswers) mustBe
          Valid(baseTransferDetails.copy(
            applicableTaxExclusions = Set(ApplicableTaxExclusions.Occupational)
          ))
      }

      "when a transfer includes a cash asset" - {
        "with a valid amount" in {
          val userAnswers = buildBaseUserAnswers
            .set(TypeOfAssetPage, Seq(TypeOfAsset.Cash)).success.value
            .set(CashAmountInTransferPage, testCashAmount).success.value

          val expected = baseTransferDetails.copy(
            typeOfAsset             = Seq(TypeOfAsset.Cash),
            cashAmountInTransfer    = Some(testCashAmount),
            applicableTaxExclusions = Set(ApplicableTaxExclusions.Occupational)
          )

          validator.fromUserAnswers(userAnswers) mustBe Valid(expected)
        }
      }

      "when a transfer includes unquoted shares" - {
        "with valid share details" in {
          val userAnswers = buildBaseUserAnswers
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
            applicableTaxExclusions = Set(ApplicableTaxExclusions.Occupational)
          )

          validator.fromUserAnswers(userAnswers) mustBe Valid(expected)
        }
      }

      "when a transfer includes quoted shares" - {
        "with valid share details" in {
          val userAnswers = buildBaseUserAnswers
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
            applicableTaxExclusions = Set(ApplicableTaxExclusions.Occupational)
          )

          validator.fromUserAnswers(userAnswers) mustBe Valid(expected)
        }
      }

      "when a transfer includes other assets" - {
        "with valid asset details" in {
          val userAnswers = buildBaseUserAnswers
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
            applicableTaxExclusions = Set(ApplicableTaxExclusions.Occupational)
          )

          validator.fromUserAnswers(userAnswers) mustBe Valid(expected)
        }
      }
    }

    "must return errors" - {
      "when required fields are missing" in {
        val result = validator.fromUserAnswers(emptyUserAnswers)

        result mustBe Invalid(TransferDetailsValidator.notStarted)
      }

      "when a cash asset is selected but the amount is missing" in {
        val userAnswers = buildBaseUserAnswers
          .set(TypeOfAssetPage, Seq(TypeOfAsset.Cash)).success.value

        val result = validator.fromUserAnswers(userAnswers)

        result mustBe Invalid(Chain(
          DataMissingError(CashAmountInTransferPage)
        ))
      }

      "when a property asset is selected but the details are missing" in {
        val userAnswers = buildBaseUserAnswers
          .set(TypeOfAssetPage, Seq(TypeOfAsset.Property)).success.value

        val result = validator.fromUserAnswers(userAnswers)

        result mustBe Invalid(Chain(
          DataMissingError(PropertyQuery)
        ))
      }

      "when unquoted shares are selected but the details are missing" in {
        val userAnswers = buildBaseUserAnswers
          .set(TypeOfAssetPage, Seq(TypeOfAsset.UnquotedShares)).success.value

        val result = validator.fromUserAnswers(userAnswers)

        result mustBe Invalid(Chain(
          DataMissingError(UnquotedSharesQuery)
        ))
      }

      "when quoted shares are selected but the details are missing" in {
        val userAnswers = buildBaseUserAnswers
          .set(TypeOfAssetPage, Seq(TypeOfAsset.QuotedShares)).success.value

        val result = validator.fromUserAnswers(userAnswers)

        result mustBe Invalid(Chain(
          DataMissingError(QuotedSharesQuery)
        ))
      }

      "when other assets are selected but the details are missing" in {
        val userAnswers = buildBaseUserAnswers
          .set(TypeOfAssetPage, Seq(TypeOfAsset.Other)).success.value

        val result = validator.fromUserAnswers(userAnswers)

        result mustBe Invalid(Chain(
          DataMissingError(OtherAssetsQuery)
        ))
      }
    }
  }
}

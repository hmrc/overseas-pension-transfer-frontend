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

package viewmodels.checkAnswers.transferDetails

import base.SpecBase
import models.ApplicableTaxExclusions.{Occupational, Publicservice, Resident}
import models.{ApplicableTaxExclusions, CheckMode, NormalMode}
import models.WhyTransferIsTaxable.TransferExceedsOTCAllowance
import models.assets.{OtherAssetsEntry, PropertyEntry, QuotedSharesEntry, TypeOfAsset, UnquotedSharesEntry}
import models.address.{Country, PropertyAddress}
import org.scalatest.freespec.AnyFreeSpec
import pages.transferDetails.assetsMiniJourneys.cash.CashAmountInTransferPage
import pages.transferDetails.assetsMiniJourneys.property.MorePropertyDeclarationPage
import pages.transferDetails._
import pages.transferDetails.assetsMiniJourneys.otherAssets.MoreOtherAssetsDeclarationPage
import pages.transferDetails.assetsMiniJourneys.quotedShares.MoreQuotedSharesDeclarationPage
import pages.transferDetails.assetsMiniJourneys.unquotedShares.MoreUnquotedSharesDeclarationPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import queries.assets.{OtherAssetsQuery, PropertyQuery, QuotedSharesQuery, UnquotedSharesQuery}
import viewmodels.checkAnswers.transferDetails.TransferDetailsSummary.rows

import java.time.LocalDate

class TransferDetailsSummarySpec extends AnyFreeSpec with SpecBase {

  implicit val messages: Messages = stubMessages()

  private val propertyEntry      =
    PropertyEntry(PropertyAddress("Line1", "Line2", None, None, None, Country("GB", "United Kingdom"), None), BigDecimal(100000.99), "A Barn")
  private val unquotedShareEntry = UnquotedSharesEntry("Unquoted Share Description", BigDecimal(1000.00), 1, "A")
  private val quotedShareEntry   = QuotedSharesEntry("Quoted Share Description", BigDecimal(1000.00), 2, "B")
  private val otherAssetEntry    = OtherAssetsEntry("Other Asset Description", BigDecimal(3000.00))

  "TransferDetailsSummary" - {
    "must return all rows when all data is available" in {
      val userAnswers = emptyUserAnswers
        .set(OverseasTransferAllowancePage, BigDecimal(1000)).success.value
        .set(AmountOfTransferPage, BigDecimal(10000)).success.value
        .set(IsTransferTaxablePage, true).success.value
        .set(WhyTransferIsTaxablePage, TransferExceedsOTCAllowance).success.value
        .set(ApplicableTaxExclusionsPage, Set[ApplicableTaxExclusions](Occupational, Publicservice, Resident)).success.value
        .set(AmountOfTaxDeductedPage, BigDecimal(1000)).success.value
        .set(NetTransferAmountPage, BigDecimal(9000)).success.value
        .set(DateOfTransferPage, LocalDate.now).success.value
        .set(IsTransferCashOnlyPage, false).success.value
        .set(
          TypeOfAssetPage,
          Seq(
            TypeOfAsset.Property,
            TypeOfAsset.UnquotedShares,
            TypeOfAsset.QuotedShares,
            TypeOfAsset.Other
          )
        ).success.value
        .set(CashAmountInTransferPage, BigDecimal(5000)).success.value
        .set(PropertyQuery, List.fill(5)(propertyEntry)).success.value
        .set(MorePropertyDeclarationPage, true).success.value
        .set(UnquotedSharesQuery, List.fill(5)(unquotedShareEntry)).success.value
        .set(MoreUnquotedSharesDeclarationPage, true).success.value
        .set(QuotedSharesQuery, List.fill(5)(quotedShareEntry)).success.value
        .set(MoreQuotedSharesDeclarationPage, true).success.value
        .set(OtherAssetsQuery, List.fill(5)(otherAssetEntry)).success.value
        .set(MoreOtherAssetsDeclarationPage, true).success.value

      val result = rows(CheckMode, userAnswers)

      val expectedRowKeys = Seq(
        "overseasTransferAllowance.checkYourAnswersLabel",
        "amountOfTransfer.checkYourAnswersLabel",
        "isTransferTaxable.checkYourAnswersLabel",
        "whyTransferIsTaxable.checkYourAnswersLabel",
        "applicableTaxExclusions.checkYourAnswersLabel",
        "amountOfTaxDeducted.checkYourAnswersLabel",
        "netTransferAmount.checkYourAnswersLabel",
        "dateOfTransfer.checkYourAnswersLabel",
        "isTransferCashOnly.checkYourAnswersLabel",
        "typeOfAsset.checkYourAnswersLabel",
        "cashAmountInTransfer.checkYourAnswersLabel",
        "propertyAmendContinue.checkYourAnswersLabel",
        "moreThanFive.properties.checkYourAnswersLabel",
        "unquotedSharesAmendContinue.checkYourAnswersLabel",
        "moreThanFive.unquotedShares.checkYourAnswersLabel",
        "quotedSharesAmendContinue.checkYourAnswersLabel",
        "moreThanFive.quotedShares.checkYourAnswersLabel",
        "otherAssetsAmendContinue.checkYourAnswersLabel",
        "moreThanFive.otherAssets.checkYourAnswersLabel"
      )
      val actualRowKeys   = result.map(_.key.content.toString)

      expectedRowKeys.foreach { key =>
        actualRowKeys.exists(_.contains(key)) mustBe true
      }
    }

    "must not include 'more than 5' rows when there are 5 or fewer assets" in {
      val userAnswers = emptyUserAnswers
        .set(OverseasTransferAllowancePage, BigDecimal(1000)).success.value
        .set(AmountOfTransferPage, BigDecimal(10000)).success.value
        .set(IsTransferTaxablePage, true).success.value
        .set(WhyTransferIsTaxablePage, TransferExceedsOTCAllowance).success.value
        .set(ApplicableTaxExclusionsPage, Set[ApplicableTaxExclusions](Occupational, Publicservice, Resident)).success.value
        .set(AmountOfTaxDeductedPage, BigDecimal(1000)).success.value
        .set(NetTransferAmountPage, BigDecimal(9000)).success.value
        .set(DateOfTransferPage, LocalDate.now).success.value
        .set(IsTransferCashOnlyPage, false).success.value
        .set(
          TypeOfAssetPage,
          Seq(
            TypeOfAsset.Property,
            TypeOfAsset.UnquotedShares,
            TypeOfAsset.QuotedShares,
            TypeOfAsset.Other
          )
        ).success.value
        .set(CashAmountInTransferPage, BigDecimal(5000)).success.value
        .set(PropertyQuery, List(propertyEntry)).success.value
        .set(MorePropertyDeclarationPage, false).success.value
        .set(UnquotedSharesQuery, List.fill(2)(unquotedShareEntry)).success.value
        .set(MoreUnquotedSharesDeclarationPage, false).success.value
        .set(QuotedSharesQuery, List.fill(3)(quotedShareEntry)).success.value
        .set(MoreQuotedSharesDeclarationPage, false).success.value
        .set(OtherAssetsQuery, List.fill(4)(otherAssetEntry)).success.value
        .set(MoreOtherAssetsDeclarationPage, false).success.value

      val result = rows(CheckMode, userAnswers)

      val expectedRowKeys = Seq(
        "overseasTransferAllowance.checkYourAnswersLabel",
        "amountOfTransfer.checkYourAnswersLabel",
        "isTransferTaxable.checkYourAnswersLabel",
        "whyTransferIsTaxable.checkYourAnswersLabel",
        "applicableTaxExclusions.checkYourAnswersLabel",
        "amountOfTaxDeducted.checkYourAnswersLabel",
        "netTransferAmount.checkYourAnswersLabel",
        "dateOfTransfer.checkYourAnswersLabel",
        "isTransferCashOnly.checkYourAnswersLabel",
        "typeOfAsset.checkYourAnswersLabel",
        "cashAmountInTransfer.checkYourAnswersLabel",
        "propertyAmendContinue.checkYourAnswersLabel",
        "unquotedSharesAmendContinue.checkYourAnswersLabel",
        "quotedSharesAmendContinue.checkYourAnswersLabel",
        "otherAssetsAmendContinue.checkYourAnswersLabel"
      )

      val unexpectedRowKeys = Seq(
        "moreThanFive.properties.checkYourAnswersLabel",
        "moreThanFive.unquotedShares.checkYourAnswersLabel",
        "moreThanFive.quotedShares.checkYourAnswersLabel",
        "moreThanFive.otherAssets.checkYourAnswersLabel"
      )

      val actualRowKeys = result.map(_.key.content.toString)

      expectedRowKeys.foreach { key =>
        actualRowKeys.exists(_.contains(key)) mustBe true
      }

      unexpectedRowKeys.foreach { key =>
        actualRowKeys.exists(_.contains(key)) mustBe false
      }
    }

    "must not include cash amount row when transfer is cash only" in {
      val userAnswers = emptyUserAnswers
        .set(IsTransferCashOnlyPage, true).success.value

      val result = rows(CheckMode, userAnswers)
      result.exists(_.key.content.toString.contains("cashAmountInTransfer")) mustBe false
    }

    "must include cash amount row when transfer is not cash only" in {
      val userAnswers = emptyUserAnswers
        .set(IsTransferCashOnlyPage, false).success.value
        .set(CashAmountInTransferPage, BigDecimal(1000)).success.value

      val result = rows(CheckMode, userAnswers)
      result.exists(_.key.content.toString.contains("cashAmountInTransfer")) mustBe true
    }

    "must handle different modes correctly" in {
      val userAnswers = emptyUserAnswers
        .set(IsTransferCashOnlyPage, false).success.value

      val checkModeResult  = rows(CheckMode, userAnswers)
      val normalModeResult = rows(NormalMode, userAnswers)

      checkModeResult.size mustBe normalModeResult.size
    }

    "must handle empty user answers" in {
      val result = rows(CheckMode, emptyUserAnswers)
      result.size must be >= 0
    }
  }
}

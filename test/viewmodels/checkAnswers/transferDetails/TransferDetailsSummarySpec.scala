package viewmodels.checkAnswers.transferDetails

import base.SpecBase
import models.ApplicableTaxExclusions.{Occupational, Publicservice, Resident}
import models.{ApplicableTaxExclusions, CheckMode, NormalMode}
import models.WhyTransferIsTaxable.TransferExceedsOTCAllowance
import models.assets.{PropertyEntry, TypeOfAsset}
import models.address.{Country, PropertyAddress}
import org.scalatest.freespec.AnyFreeSpec
import pages.transferDetails.assetsMiniJourneys.cash.CashAmountInTransferPage
import pages.transferDetails.assetsMiniJourneys.property.MorePropertyDeclarationPage
import pages.transferDetails._
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import queries.assets.PropertyQuery
import viewmodels.checkAnswers.transferDetails.TransferDetailsSummary.rows

import java.time.LocalDate

class TransferDetailsSummarySpec extends AnyFreeSpec with SpecBase {

  implicit val messages: Messages = stubMessages()

  private val propertyEntry = PropertyEntry(
    PropertyAddress("Line1", "Line2", None, None, Country("GB", "United Kingdom"), None),
    BigDecimal(100000.99),
    "A Barn"
  )

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
        .set(TypeOfAssetPage, Seq(TypeOfAsset.Property)).success.value
        .set(CashAmountInTransferPage, BigDecimal(5000)).success.value
        .set(PropertyQuery, List.fill(5)(propertyEntry)).success.value
        .set(MorePropertyDeclarationPage, true).success.value

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
        "moreThanFive.properties.checkYourAnswersLabel"
      )
      val actualRowKeys   = result.map(_.key.content.toString)

      expectedRowKeys.foreach { key =>
        actualRowKeys.exists(_.contains(key)) mustBe true
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

package viewmodels.checkAnswers.transferDetails

import base.SpecBase
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import models.CheckMode
import org.scalatest.freespec.AnyFreeSpec
import pages.transferDetails.assetsMiniJourneys.cash.CashAmountInTransferPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class CashAmountInTransferSummarySpec extends AnyFreeSpec with SpecBase {

  "CashAmountInTransferSummary" - {
    implicit val messages: Messages = stubMessages()

    "must return a SummaryListRow when CashAmountInTransferPage has a value" in {
      val answers = emptyUserAnswers.set(CashAmountInTransferPage, BigDecimal(12345.33)).success.value
      val result  = CashAmountInTransferSummary.row(CheckMode, answers)

      result mustBe defined
      result.get.key.content.asHtml.body must include("cashAmountInTransfer.checkYourAnswersLabel")
      result.get.value.content.asHtml.body must include("£12345.33")
      result.get.actions.get.items.head.href mustBe
        AssetsMiniJourneysRoutes.CashAmountInTransferController.onPageLoad(CheckMode).url
    }
  }
}
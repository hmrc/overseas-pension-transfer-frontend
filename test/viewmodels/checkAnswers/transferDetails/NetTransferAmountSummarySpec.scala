package viewmodels.checkAnswers.transferDetails

import base.SpecBase
import models.CheckMode
import org.scalatest.freespec.AnyFreeSpec
import pages.transferDetails.NetTransferAmountPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class NetTransferAmountSummarySpec extends AnyFreeSpec with SpecBase {

  "NetTransferAmountSummary" - {
    implicit val messages: Messages = stubMessages()

    "must return a SummaryListRow when NetTransferAmountPage has a value" in {
      val answers = emptyUserAnswers.set(NetTransferAmountPage, BigDecimal(12345.33)).success.value
      val result  = NetTransferAmountSummary.row(CheckMode, answers)

      result mustBe defined
      result.get.key.content.asHtml.body must include("netTransferAmount.checkYourAnswersLabel")
      result.get.value.content.asHtml.body must include("£12345.33")
      result.get.actions.get.items.head.href mustBe
        controllers.transferDetails.routes.NetTransferAmountController.onPageLoad(CheckMode).url
    }
  }
}

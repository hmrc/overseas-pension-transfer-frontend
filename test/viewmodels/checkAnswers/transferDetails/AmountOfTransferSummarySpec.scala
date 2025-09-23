package viewmodels.checkAnswers.transferDetails

import base.SpecBase
import models.CheckMode
import org.scalatest.freespec.AnyFreeSpec
import pages.transferDetails.AmountOfTransferPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class AmountOfTransferSummarySpec extends AnyFreeSpec with SpecBase {

  "AmountOfTransferSummary" - {
    implicit val messages: Messages = stubMessages()

    "must return a SummaryListRow when AmountOfTransferPage has a value" in {
      val answers = emptyUserAnswers.set(AmountOfTransferPage, BigDecimal(12345.33)).success.value
      val result  = AmountOfTransferSummary.row(CheckMode, answers)

      result mustBe defined
      result.get.key.content.asHtml.body must include("amountOfTransfer.checkYourAnswersLabel")
      result.get.value.content.asHtml.body must include("£12345.33")
      result.get.actions.get.items.head.href mustBe
        controllers.transferDetails.routes.AmountOfTransferController.onPageLoad(CheckMode).url
    }
  }
}
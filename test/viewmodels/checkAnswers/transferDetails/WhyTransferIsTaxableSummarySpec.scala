package viewmodels.checkAnswers.transferDetails

import base.SpecBase
import models.CheckMode
import models.WhyTransferIsTaxable.TransferExceedsOTCAllowance
import org.scalatest.freespec.AnyFreeSpec
import pages.transferDetails.WhyTransferIsTaxablePage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class WhyTransferIsTaxableSummarySpec extends AnyFreeSpec with SpecBase {

  "WhyTransferIsTaxableSummary" - {
    implicit val messages: Messages = stubMessages()

    "must return a SummaryListRow when WhyTransferIsTaxablePage has a value" in {
      val answers = emptyUserAnswers.set(WhyTransferIsTaxablePage, TransferExceedsOTCAllowance).success.value
      val result  = WhyTransferIsTaxableSummary.row(CheckMode, answers)

      result mustBe defined
      result.get.key.content.asHtml.body must include("whyTransferIsTaxable.checkYourAnswersLabel")
      result.get.value.content.asHtml.body must include("transferExceedsOTCAllowance")
      result.get.actions.get.items.head.href mustBe
        controllers.transferDetails.routes.WhyTransferIsTaxableController.onPageLoad(CheckMode).url
    }
  }
}

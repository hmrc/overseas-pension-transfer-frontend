package viewmodels.checkAnswers.transferDetails

import base.SpecBase
import models.CheckMode
import org.scalatest.freespec.AnyFreeSpec
import pages.transferDetails.OverseasTransferAllowancePage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class OverseasTransferAllowanceSummarySpec extends AnyFreeSpec with SpecBase {

  "OverseasTransferAllowanceSummary" - {
    implicit val messages: Messages = stubMessages()

    "must return a SummaryListRow when OverseasTransferAllowancePage has a value" in {
      val answers = emptyUserAnswers.set(OverseasTransferAllowancePage, BigDecimal(12345.33)).success.value
      val result  = OverseasTransferAllowanceSummary.row(CheckMode, answers)

      result mustBe defined
      result.get.key.content.asHtml.body must include("overseasTransferAllowance.checkYourAnswersLabel")
      result.get.value.content.asHtml.body must include("£12345.33")
      result.get.actions.get.items.head.href mustBe
        controllers.transferDetails.routes.OverseasTransferAllowanceController.onPageLoad(CheckMode).url
    }
  }
}

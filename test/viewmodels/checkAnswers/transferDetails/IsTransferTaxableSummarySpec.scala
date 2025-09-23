package viewmodels.checkAnswers.transferDetails

import base.SpecBase
import models.CheckMode
import org.scalatest.freespec.AnyFreeSpec
import pages.transferDetails.IsTransferTaxablePage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class IsTransferTaxableSummarySpec extends AnyFreeSpec with SpecBase {

  "IsTransferTaxableSummary" - {
    implicit val messages: Messages = stubMessages()

    "must return a SummaryListRow when IsTransferTaxablePage has a value" in {
      val answers = emptyUserAnswers.set(IsTransferTaxablePage, false).success.value
      val result  = IsTransferTaxableSummary.row(CheckMode, answers)

      result mustBe defined
      result.get.key.content.asHtml.body must include("isTransferTaxable.checkYourAnswersLabel")
      result.get.value.content.asHtml.body must include("site.no")
      result.get.actions.get.items.head.href mustBe
        controllers.transferDetails.routes.IsTransferTaxableController.onPageLoad(CheckMode).url
    }
  }
}

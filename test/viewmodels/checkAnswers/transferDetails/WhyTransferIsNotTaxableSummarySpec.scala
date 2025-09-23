package viewmodels.checkAnswers.transferDetails

import base.SpecBase
import models.CheckMode
import models.WhyTransferIsNotTaxable.IndividualIsEmployeeOccupational
import org.scalatest.freespec.AnyFreeSpec
import pages.transferDetails.WhyTransferIsNotTaxablePage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class WhyTransferIsNotTaxableSummarySpec extends AnyFreeSpec with SpecBase {

  "WhyTransferIsNotTaxableSummary" - {
    implicit val messages: Messages = stubMessages()

    "must return a SummaryListRow when WhyTransferIsNotTaxablePage has a value" in {
      val answers = emptyUserAnswers.set(WhyTransferIsNotTaxablePage, Seq(IndividualIsEmployeeOccupational)).success.value
      val result  = WhyTransferIsNotTaxableSummary.row(CheckMode, answers)

      result mustBe defined
      result.get.key.content.asHtml.body must include("whyTransferIsNotTaxable.checkYourAnswersLabel")
      result.get.value.content.asHtml.body must include("individualIsEmployeeOccupational")
      result.get.actions.get.items.head.href mustBe
        controllers.transferDetails.routes.WhyTransferIsNotTaxableController.onPageLoad(CheckMode).url
    }
  }
}

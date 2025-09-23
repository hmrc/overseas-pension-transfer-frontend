package viewmodels.checkAnswers.transferDetails

import base.SpecBase
import models.CheckMode
import org.scalatest.freespec.AnyFreeSpec
import pages.transferDetails.AmountOfTaxDeductedPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class AmountOfTaxDeductedSummarySpec extends AnyFreeSpec with SpecBase {

  "AmountOfTaxDeductedSummary" - {
    implicit val messages: Messages = stubMessages()

    "must return a SummaryListRow when AmountOfTaxDeductedPage has a value" in {
      val answers = emptyUserAnswers.set(AmountOfTaxDeductedPage, BigDecimal(12345.33)).success.value
      val result  = AmountOfTaxDeductedSummary.row(CheckMode, answers)

      result mustBe defined
      result.get.key.content.asHtml.body must include("amountOfTaxDeducted.checkYourAnswersLabel")
      result.get.value.content.asHtml.body must include("£12345.33")
      result.get.actions.get.items.head.href mustBe
        controllers.transferDetails.routes.AmountOfTaxDeductedController.onPageLoad(CheckMode).url
    }
  }
}

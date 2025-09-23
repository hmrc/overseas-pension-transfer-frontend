package viewmodels.checkAnswers.transferDetails

import base.SpecBase
import models.CheckMode
import org.scalatest.freespec.AnyFreeSpec
import pages.transferDetails.DateOfTransferPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

import java.time.LocalDate

class DateOfTransferSummarySpec extends AnyFreeSpec with SpecBase {

  "DateOfTransferSummary" - {
    implicit val messages: Messages = stubMessages()

    "must return a SummaryListRow when DateOfTransferPage has a value" in {
      val answers = emptyUserAnswers.set(DateOfTransferPage, LocalDate.of(2025, 12, 12)).success.value
      val result  = DateOfTransferSummary.row(CheckMode, answers)

      result mustBe defined
      result.get.key.content.asHtml.body must include("dateOfTransfer.checkYourAnswersLabel")
      result.get.value.content.asHtml.body must include("12 12 2025")
      result.get.actions.get.items.head.href mustBe
        controllers.transferDetails.routes.DateOfTransferController.onPageLoad(CheckMode).url
    }
  }
}

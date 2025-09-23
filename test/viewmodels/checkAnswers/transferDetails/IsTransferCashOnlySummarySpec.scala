package viewmodels.checkAnswers.transferDetails

import base.SpecBase
import models.CheckMode
import org.scalatest.freespec.AnyFreeSpec
import pages.transferDetails.IsTransferCashOnlyPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class IsTransferCashOnlySummarySpec extends AnyFreeSpec with SpecBase {

  "IsTransferCashOnlySummary" - {
    implicit val messages: Messages = stubMessages()

    "must return a SummaryListRow when IsTransferCashOnlyPage has a value" in {
      val answers = emptyUserAnswers.set(IsTransferCashOnlyPage, false).success.value
      val result  = IsTransferCashOnlySummary.row(CheckMode, answers)

      result mustBe defined
      result.get.key.content.asHtml.body must include("isTransferCashOnly.checkYourAnswersLabel")
      result.get.value.content.asHtml.body must include("site.no")
      result.get.actions.get.items.head.href mustBe
        controllers.transferDetails.routes.IsTransferCashOnlyController.onPageLoad(CheckMode).url
    }
  }
}

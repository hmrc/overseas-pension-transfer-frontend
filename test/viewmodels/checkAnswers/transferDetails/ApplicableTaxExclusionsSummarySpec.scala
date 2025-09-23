package viewmodels.checkAnswers.transferDetails

import base.SpecBase
import models.ApplicableTaxExclusions.{Occupational, Publicservice, Resident}
import models.CheckMode
import org.scalatest.freespec.AnyFreeSpec
import pages.transferDetails.ApplicableTaxExclusionsPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class ApplicableTaxExclusionsSummarySpec extends AnyFreeSpec with SpecBase {

  "ApplicableTaxExclusionsSummary" - {
    implicit val messages: Messages = stubMessages()

    "must return a SummaryListRow when ApplicableTaxExclusionsPage has a value" in {
      val answers = emptyUserAnswers.set(ApplicableTaxExclusionsPage, Seq(Occupational, Publicservice, Resident)).success.value
      val result  = ApplicableTaxExclusionsSummary.row(CheckMode, answers)

      result mustBe defined
      result.get.key.content.asHtml.body must include("applicableTaxExclusions.checkYourAnswersLabel")
      result.get.value.content.asHtml.body must include("occupational<br>publicService<br>resident")
      result.get.actions.get.items.head.href mustBe
        controllers.transferDetails.routes.ApplicableTaxExclusionsController.onPageLoad(CheckMode).url
    }
  }
}

package viewmodels.checkAnswers.transferDetails

import base.SpecBase
import models.CheckMode
import models.assets.TypeOfAsset.{Cash, Property}
import org.scalatest.freespec.AnyFreeSpec
import pages.transferDetails.TypeOfAssetPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class TypeOfAssetSummarySpec extends AnyFreeSpec with SpecBase {

  "TypeOfAssetSummary" - {
    implicit val messages: Messages = stubMessages()

    "must return a SummaryListRow when TypeOfAssetPage has a value" in {
      val answers = emptyUserAnswers.set(TypeOfAssetPage, Seq(Cash, Property)).success.value
      val result  = TypeOfAssetSummary.row(CheckMode, answers)

      result mustBe defined
      result.get.key.content.asHtml.body must include("typeOfAsset.checkYourAnswersLabel")
      result.get.value.content.asHtml.body must include("Cash<br>Property")
      result.get.actions.get.items.head.href mustBe
        controllers.transferDetails.routes.TypeOfAssetController.onPageLoad(CheckMode).url
    }
  }
}

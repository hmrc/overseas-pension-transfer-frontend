/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package viewmodels.checkAnswers.transferDetails

import base.SpecBase
import models.CheckMode
import models.assets.TypeOfAsset.{Cash, Property}
import org.scalatest.freespec.AnyFreeSpec
import pages.transferDetails.TypeOfAssetPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

class TypeOfAssetSummarySpec extends AnyFreeSpec with SpecBase {

  "TypeOfAssetSummary" - {
    implicit val messages: Messages = stubMessages()

    "must return a SummaryListRow when TypeOfAssetPage has a value other than Cash only" in {
      val answers = emptyUserAnswers.set(TypeOfAssetPage, Seq(Cash, Property)).success.value
      val result  = TypeOfAssetSummary.row(CheckMode, answers)

      result mustBe defined
      result.get.key.content mustBe Text(messages("typeOfAsset.checkYourAnswersLabel"))
      result.get.value.content mustBe HtmlContent(messages("typeOfAsset.cash,<br>typeOfAsset.propertyAssets"))
      result.get.actions.get.items.head.href mustBe
        controllers.transferDetails.routes.TypeOfAssetController.onPageLoad(CheckMode).url
    }

    "must not return a SummaryListRow when TypeOfAssetPage has only Cash as the value" in {
      val answers = emptyUserAnswers.set(TypeOfAssetPage, Seq(Cash)).success.value
      val result  = TypeOfAssetSummary.row(CheckMode, answers)

      result mustBe None
    }
  }
}

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

package viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.otherAssets

import base.SpecBase
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import models.CheckMode
import models.assets.OtherAssetsEntry
import org.scalatest.freespec.AnyFreeSpec
import pages.transferDetails.assetsMiniJourneys.otherAssets.MoreOtherAssetsDeclarationPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import queries.assets.OtherAssetsQuery
import uk.gov.hmrc.govukfrontend.views.Aliases.Text

class OtherAssetsAmendContinueSummarySpec extends AnyFreeSpec with SpecBase {

  "OtherAssetsDescriptionSummary" - {
    implicit val messages: Messages = stubMessages()

    "must return a SummaryListRow when OtherAssetsQuery has a value" in {
      val answers = emptyUserAnswers.set(OtherAssetsQuery, List(OtherAssetsEntry("description", BigDecimal(1000.00)))).success.value
      val result  = OtherAssetsAmendContinueSummary.row(CheckMode, answers)

      result mustBe defined
      result.get.key.content mustBe Text(messages("otherAssetsAmendContinue.checkYourAnswersLabel"))
      result.get.value.content mustBe Text(messages("otherAssetsAmendContinue.summary.value"))
      result.get.actions.get.items.head.href mustBe
        AssetsMiniJourneysRoutes.OtherAssetsAmendContinueController.onPageLoad(CheckMode).url
    }

    "moreThanFiveOtherAssetsRow" - {
      "should return Some when user has selected more than 5 other assets" in {
        val userAnswers = emptyUserAnswers.set(MoreOtherAssetsDeclarationPage, true).success.value
        val result      = OtherAssetsAmendContinueSummary.moreThanFiveOtherAssetsRow(CheckMode, userAnswers, true)
        result mustBe defined
      }

      "should return None when user has not selected more than 5 other assets" in {
        val userAnswers = emptyUserAnswers.set(MoreOtherAssetsDeclarationPage, false).success.value
        val result      = OtherAssetsAmendContinueSummary.moreThanFiveOtherAssetsRow(CheckMode, userAnswers, true)
        result mustBe None
      }
    }
  }
}

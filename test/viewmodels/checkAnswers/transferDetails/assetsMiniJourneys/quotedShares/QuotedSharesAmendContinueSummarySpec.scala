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

package viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.quotedShares

import base.SpecBase
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import models.CheckMode
import models.assets.QuotedSharesEntry
import org.scalatest.freespec.AnyFreeSpec
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import queries.assets.QuotedSharesQuery
import uk.gov.hmrc.govukfrontend.views.Aliases.Text

class QuotedSharesAmendContinueSummarySpec extends AnyFreeSpec with SpecBase {

  "QuotedSharesAmendContinueSummary" - {
    implicit val messages: Messages = stubMessages()

    "must return a SummaryListRow when QuotedSharesQuery has a value" in {
      val answers = emptyUserAnswers.set(
        QuotedSharesQuery,
        List(
          QuotedSharesEntry(
            "Name",
            BigDecimal(9876543.00),
            12,
            "class"
          )
        )
      ).success.value

      val result = QuotedSharesAmendContinueSummary.row(CheckMode, answers)

      result mustBe defined
      result.get.key.content mustBe Text(messages("quotedSharesAmendContinue.checkYourAnswersLabel"))
      result.get.value.content mustBe Text(messages("quotedSharesAmendContinue.summary.value"))
      result.get.actions.get.items.head.href mustBe
        AssetsMiniJourneysRoutes.QuotedSharesAmendContinueController.onPageLoad(CheckMode).url
    }
  }
}

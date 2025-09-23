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

package viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.unquotedShares

import base.SpecBase
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import models.CheckMode
import models.assets.UnquotedSharesEntry
import org.scalatest.freespec.AnyFreeSpec
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import queries.assets.UnquotedSharesQuery

class UnquotedSharesAmendContinueSummarySpec extends AnyFreeSpec with SpecBase {

  "UnquotedSharesAmendContinueSummary" - {
    implicit val messages: Messages = stubMessages()

    "must return a SummaryListRow when UnquotedSharesQuery has a value" in {
      val answers = emptyUserAnswers.set(
        UnquotedSharesQuery,
        List(
          UnquotedSharesEntry(
            "Name",
            BigDecimal(9876543.00),
            12,
            "class"
          )
        )
      ).success.value

      val result = UnquotedSharesAmendContinueSummary.row(CheckMode, answers)

      result mustBe defined
      result.get.key.content.asHtml.body must include("unquotedSharesAmendContinue.checkYourAnswersLabel")
      result.get.value.content.asHtml.body must include("unquotedSharesAmendContinue.summary.value")
      result.get.actions.get.items.head.href mustBe
        AssetsMiniJourneysRoutes.UnquotedSharesAmendContinueController.onPageLoad(CheckMode).url
    }
  }
}

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

package viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.property

import base.SpecBase
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import models.CheckMode
import models.address.{Country, PropertyAddress}
import models.assets.PropertyEntry
import org.scalatest.freespec.AnyFreeSpec
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import queries.assets.PropertyQuery

class PropertyAmendContinueSummarySpec extends AnyFreeSpec with SpecBase {

  "PropertyAmendContinueSummary" - {
    implicit val messages: Messages = stubMessages()

    "must return a SummaryListRow when PropertyAmendContinueQuery has a value" in {
      val answers = emptyUserAnswers.set(
        PropertyQuery,
        List(
          PropertyEntry(
            PropertyAddress("Line1", "Line2", None, None, Country("GB", "United Kingdom"), None),
            BigDecimal(100000.99),
            "Big House"
          )
        )
      ).success.value

      val result = PropertyAmendContinueSummary.row(CheckMode, answers)

      result mustBe defined
      result.get.key.content.asHtml.body must include("propertyAmendContinue.checkYourAnswersLabel")
      result.get.value.content.asHtml.body must include("propertyAmendContinue.summary.value")
      result.get.actions.get.items.head.href mustBe
        AssetsMiniJourneysRoutes.PropertyAmendContinueController.onPageLoad(CheckMode).url
    }
  }
}

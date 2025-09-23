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
import org.scalatest.freespec.AnyFreeSpec
import pages.transferDetails.assetsMiniJourneys.property.PropertyDescriptionPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class PropertyDescriptionSummarySpec extends AnyFreeSpec with SpecBase {

  "PropertyDescriptionSummary" - {
    implicit val messages: Messages = stubMessages()

    "must return a SummaryListRow when PropertyDescriptionPage has a value" in {
      val answers = emptyUserAnswers.set(PropertyDescriptionPage(0), "Property Description").success.value
      val result  = PropertyDescriptionSummary.row(CheckMode, answers, 0)

      result mustBe defined
      result.get.key.content.asHtml.body must include("propertyDescription.checkYourAnswersLabel")
      result.get.value.content.asHtml.body must include("Property Description")
      result.get.actions.get.items.head.href mustBe
        AssetsMiniJourneysRoutes.PropertyDescriptionController.onPageLoad(CheckMode, 0).url
    }
  }
}

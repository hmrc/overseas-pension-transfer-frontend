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
import pages.transferDetails.assetsMiniJourneys.property.PropertyValuePage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.otherAssets.OtherAssetsDescriptionSummary

class PropertyValueSummarySpec extends AnyFreeSpec with SpecBase {

  "PropertyValueSummary" - {
    implicit val messages: Messages = stubMessages()

    "must return a SummaryListRow when PropertyValuePage has a value" in {
      val answers = emptyUserAnswers.set(PropertyValuePage(0), BigDecimal(12345.99)).success.value
      val result  = PropertyValueSummary.row(CheckMode, answers, 0)

      result mustBe defined
      result.get.key.content mustBe Text(messages("propertyValue.checkYourAnswersLabel"))
      result.get.value.content mustBe Text("£12,345.99")
      result.get.actions.get.items.head.href mustBe
        AssetsMiniJourneysRoutes.PropertyValueController.onPageLoad(CheckMode, 0).url
    }
  }
}

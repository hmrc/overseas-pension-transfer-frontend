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

package viewmodels.checkAnswers.qropsDetails

import base.SpecBase
import models.CheckMode
import org.scalatest.freespec.AnyFreeSpec
import pages.qropsDetails.QROPSOtherCountryPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class QROPSOtherCountrySummarySpec extends AnyFreeSpec with SpecBase {

  "QROPSOtherCountrySummary" - {
    implicit val messages: Messages = stubMessages()

    "must return a SummaryListRow when QROPSOtherCountryPage has a value" in {
      val answers = emptyUserAnswers.set(QROPSOtherCountryPage, "Other Country").success.value
      val result  = QROPSOtherCountrySummary.row(CheckMode, answers)

      result mustBe defined
      result.get.key.content mustBe Text(messages("qropsOtherCountry.checkYourAnswersLabel"))
      result.get.value.content mustBe Text("Other Country")
      result.get.actions.get.items.head.href mustBe
        controllers.qropsDetails.routes.QROPSCountryController.onPageLoad(CheckMode).url
    }
  }
}

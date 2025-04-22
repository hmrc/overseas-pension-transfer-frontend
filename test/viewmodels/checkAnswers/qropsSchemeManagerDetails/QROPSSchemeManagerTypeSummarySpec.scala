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

package viewmodels.checkAnswers.qropsSchemeManagerDetails

import base.SpecBase
import models.QROPSSchemeManagerType
import pages.QROPSSchemeManagerTypePage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class QROPSSchemeManagerTypeSummarySpec extends SpecBase {

  "QROPS scheme manager type Summary" - {
    implicit val messages: Messages = stubMessages()

    "must return a SummaryListRow when QROPSSchemeManagerTypePage has a value" in {
      val answers = emptyUserAnswers.set(QROPSSchemeManagerTypePage, QROPSSchemeManagerType.Individual).success.value
      val result  = QROPSSchemeManagerTypeSummary.row(answers)

      result mustBe defined
      result.get.key.content.asHtml.body must include("qropsSchemeManagerType.checkYourAnswersLabel")
      result.get.value.content.asHtml.body must include(QROPSSchemeManagerType.Individual.toString)
    }
  }
}

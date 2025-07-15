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
import models.{PersonName, SchemeManagersName}
import org.scalatest.freespec.AnyFreeSpec
import pages.qropsSchemeManagerDetails.SchemeManagersNamePage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class SchemeManagersNameSummarySpec extends AnyFreeSpec with SpecBase {

  "Scheme manager's name Summary" - {
    implicit val messages: Messages = stubMessages()

    "must return a SummaryListRow when SchemeManagersNamePage has a value" in {
      val answers = emptyUserAnswers.set(SchemeManagersNamePage, PersonName("FirstName", "LastName")).success.value
      val result  = SchemeManagersNameSummary.row(answers)

      result mustBe defined
      result.get.key.content.asHtml.body must include("schemeManagersName.checkYourAnswersLabel")
      result.get.value.content.asHtml.body must include("FirstName LastName")
    }
  }
}

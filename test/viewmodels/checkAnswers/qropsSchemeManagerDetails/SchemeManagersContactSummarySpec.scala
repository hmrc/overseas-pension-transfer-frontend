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
import models.CheckMode
import org.scalatest.freespec.AnyFreeSpec
import pages.qropsSchemeManagerDetails.SchemeManagersContactPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, Text}

class SchemeManagersContactSummarySpec extends AnyFreeSpec with SpecBase {

  "SchemeManagersContact" - {
    implicit val messages: Messages = stubMessages()

    "must return a SummaryListRow when SchemeManagersContactPage has a value" in {
      val phoneNumber = "+441234567890"
      val answers     = emptyUserAnswers.set(SchemeManagersContactPage, phoneNumber).success.value
      val result      = SchemeManagersContactSummary.row(CheckMode, answers)

      result mustBe defined
      result.get.key.content mustBe Text(messages("schemeManagersContact.checkYourAnswersLabel"))

      result.get.value.content mustBe HtmlContent(
        s"""<span aria-hidden="true">$phoneNumber</span>""" +
          s"""<span class="govuk-visually-hidden">+4 4 1 2 3 4 5 6 7 8 9 0</span>"""
      )

      result.get.actions.get.items.head.href mustBe
        controllers.qropsSchemeManagerDetails.routes.SchemeManagersContactController.onPageLoad(CheckMode).url
    }
  }
}

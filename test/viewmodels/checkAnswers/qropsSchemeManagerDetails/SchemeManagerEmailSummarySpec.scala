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
import pages.qropsSchemeManagerDetails.SchemeManagersEmailPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class SchemeManagerEmailSummarySpec extends AnyFreeSpec with SpecBase {

  "Scheme Manager Email" - {
    implicit val messages: Messages = stubMessages()

    "must return a SummaryListRow when SchemeManagerEmail has a value" in {
      val answers = emptyUserAnswers.set(SchemeManagersEmailPage, "Scheme Manager Sample Email").success.value
      val result  = SchemeManagersEmailSummary.row(CheckMode, answers)

      result mustBe defined
      result.get.key.content.asHtml.body must include("schemeManagersEmail.checkYourAnswersLabel")
      result.get.value.content.asHtml.body must include("Scheme Manager Sample Email")
      result.get.actions.get.items.head.href mustBe
        controllers.qropsSchemeManagerDetails.routes.SchemeManagersEmailController.onPageLoad(CheckMode).url
    }
  }
}

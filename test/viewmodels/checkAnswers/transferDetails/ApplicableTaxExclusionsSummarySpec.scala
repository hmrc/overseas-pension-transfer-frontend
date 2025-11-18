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

package viewmodels.checkAnswers.transferDetails

import base.SpecBase
import models.ApplicableTaxExclusions.{Occupational, Publicservice, Resident}
import models.{ApplicableTaxExclusions, CheckMode}
import org.scalatest.freespec.AnyFreeSpec
import pages.transferDetails.ApplicableTaxExclusionsPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

class ApplicableTaxExclusionsSummarySpec extends AnyFreeSpec with SpecBase {

  "ApplicableTaxExclusionsSummary" - {
    implicit val messages: Messages = stubMessages()

    "must return a SummaryListRow when ApplicableTaxExclusionsPage has a value" in {
      val answers = emptyUserAnswers.set(ApplicableTaxExclusionsPage, Set[ApplicableTaxExclusions](Occupational, Publicservice, Resident)).success.value
      val result  = ApplicableTaxExclusionsSummary.row(CheckMode, answers)

      result mustBe defined
      result.get.key.content mustBe Text(messages("applicableTaxExclusions.checkYourAnswersLabel"))
      result.get.value.content mustBe HtmlContent(messages(
        "applicableTaxExclusions.occupational<br><br>applicableTaxExclusions.publicService<br><br>applicableTaxExclusions.resident"
      ))
      result.get.actions.get.items.head.href mustBe
        controllers.transferDetails.routes.ApplicableTaxExclusionsController.onPageLoad(CheckMode).url
    }
  }
}

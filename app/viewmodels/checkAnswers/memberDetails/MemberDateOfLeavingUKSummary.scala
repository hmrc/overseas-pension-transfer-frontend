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

package viewmodels.checkAnswers.memberDetails

import viewmodels.implicits._
import utils.DateTimeFormats.dateTimeFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import models.Mode
import models.UserAnswers
import pages.memberDetails.MemberDateOfLeavingUKPage
import play.api.i18n.Lang
import play.api.i18n.Messages
import viewmodels.govuk.summarylist._

object MemberDateOfLeavingUKSummary {

  def row(mode: Mode, answers: UserAnswers, showChangeLink: Boolean = true)(implicit
    messages: Messages
  ): Option[SummaryListRow] =
    answers.get(MemberDateOfLeavingUKPage).map { answer =>
      implicit val lang: Lang = messages.lang

      val actions =
        if (showChangeLink) {
          Seq(
            ActionItemViewModel("site.change", MemberDateOfLeavingUKPage.changeLink(mode).url)
              .withVisuallyHiddenText(messages("memberDateOfLeavingUK.change.hidden"))
          )
        } else {
          Seq.empty
        }

      SummaryListRowViewModel(
        key = "memberDateOfLeavingUK.checkYourAnswersLabel",
        value = ValueViewModel(answer.format(dateTimeFormat)),
        actions = actions
      )
    }

}

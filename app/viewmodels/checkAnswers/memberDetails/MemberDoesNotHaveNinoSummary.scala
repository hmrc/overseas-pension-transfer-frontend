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

import models.{Mode, UserAnswers}
import pages.memberDetails.MemberDoesNotHaveNinoPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object MemberDoesNotHaveNinoSummary {

  def row(
      mode: Mode,
      answers: UserAnswers,
      showChangeLink: Boolean           = true,
      additionalClasses: Option[String] = None
    )(implicit messages: Messages
    ): Option[SummaryListRow] =
    answers.get(MemberDoesNotHaveNinoPage).map {

      val actions = if (showChangeLink) {
        Seq(
          ActionItemViewModel("site.change", MemberDoesNotHaveNinoPage.changeLink(mode).url)
            .withVisuallyHiddenText(messages("memberDoesNotHaveNino.change.hidden"))
        )
      } else {
        Seq.empty
      }

      answer =>
        SummaryListRowViewModel(
          key     = "memberDoesNotHaveNino.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlFormat.escape(answer).toString),
          actions = actions
        ).withCssClass(additionalClasses.getOrElse(""))
    }
}

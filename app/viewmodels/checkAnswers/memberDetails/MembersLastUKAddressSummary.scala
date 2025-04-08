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

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.MembersLastUKAddressPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object MembersLastUKAddressSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(MembersLastUKAddressPage).map {
      answer =>
        val value = Seq(
          Some(answer.line1),
          Some(answer.line2),
          answer.line3,
          answer.line4,
          answer.postcode
        ).flatMap {
          case Some(part) if !part.trim.isEmpty => Some(HtmlFormat.escape(part))
          case _                                => None
        }.mkString("<br>")

        SummaryListRowViewModel(
          key     = "membersLastUKAddress.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlContent(value)),
          actions = Seq(
            ActionItemViewModel("site.change", routes.MembersLastUKAddressController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("membersLastUKAddress.change.hidden"))
          )
        )
    }
}

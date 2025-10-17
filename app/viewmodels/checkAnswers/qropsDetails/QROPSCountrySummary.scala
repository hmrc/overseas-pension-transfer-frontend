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

import models.{Mode, UserAnswers}
import pages.qropsDetails.QROPSCountryPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object QROPSCountrySummary {

  def row(mode: Mode, answers: UserAnswers, showChangeLink: Boolean = true)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(QROPSCountryPage).map { country =>
      val actions =
        if (showChangeLink) {
          Seq(
            ActionItemViewModel("site.change", QROPSCountryPage.changeLink(mode).url)
              .withVisuallyHiddenText(messages("qropsCountry.change.hidden"))
          )
        } else {
          Seq.empty
        }

      SummaryListRowViewModel(
        key     = "qropsCountry.checkYourAnswersLabel",
        value   = ValueViewModel(HtmlFormat.escape(country.name).toString),
        actions = actions
      )
    }

}

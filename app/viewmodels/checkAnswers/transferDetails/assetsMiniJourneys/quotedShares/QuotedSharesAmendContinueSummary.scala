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

package viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.quotedShares

import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import models.{Mode, UserAnswers}
import play.api.i18n.Messages
import queries.assets.QuotedSharesQuery
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.addtoalist.ListItem
import utils.AppUtils
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object QuotedSharesAmendContinueSummary extends AppUtils {

  def row(mode: Mode, userAnswers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {

    val answers   = userAnswers.get(QuotedSharesQuery)
    val valueText = messages("quotedSharesAmendContinue.summary.value", answers.size)

    answers match {
      case Some(entries) if entries.nonEmpty =>
        Some(
          SummaryListRowViewModel(
            key     = "quotedSharesAmendContinue.checkYourAnswersLabel",
            value   = ValueViewModel(valueText),
            actions = Seq(
              ActionItemViewModel("site.change", AssetsMiniJourneysRoutes.QuotedSharesAmendContinueController.onPageLoad(mode).url)
                .withVisuallyHiddenText(messages("quotedSharesAmendContinue.change.hidden"))
            )
          )
        )
      case _                                 => None
    }
  }

  def rows(answers: UserAnswers): Seq[ListItem] = {
    val maybeEntries = answers.get(QuotedSharesQuery)
    maybeEntries.getOrElse(Nil).zipWithIndex.map {
      case (entry, index) =>
        ListItem(
          name      = entry.companyName,
          changeUrl = AssetsMiniJourneysRoutes.QuotedSharesCYAController.onPageLoad(index).url,
          removeUrl = AssetsMiniJourneysRoutes.QuotedSharesConfirmRemovalController.onPageLoad(index).url
        )
    }
  }
}

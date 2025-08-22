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

package viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.unquotedShares

import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import models.{CheckMode, Mode, UserAnswers}
import play.api.i18n.Messages
import queries.assets.UnquotedSharesQuery
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._
import utils.AppUtils
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.addtoalist.ListItem

object UnquotedSharesAmendContinueSummary extends AppUtils {

  def row(mode: Mode, userAnswers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {

    val answers   = userAnswers.get(UnquotedSharesQuery)
    val valueText = messages("unquotedSharesAmendContinue.summary.value", answers.size)

    answers match {
      case Some(entries) if entries.nonEmpty =>
        Some(
          SummaryListRowViewModel(
            key     = "unquotedSharesAmendContinue.checkYourAnswersLabel",
            value   = ValueViewModel(valueText),
            actions = Seq(
              ActionItemViewModel("site.change", AssetsMiniJourneysRoutes.UnquotedSharesAmendContinueController.onPageLoad(mode).url)
                .withVisuallyHiddenText(messages("unquotedSharesAmendContinue.change.hidden"))
            )
          )
        )
      case _                                 => None
    }
  }

  def rows(answers: UserAnswers): Seq[ListItem] = {
    val maybeEntries = answers.get(UnquotedSharesQuery)
    maybeEntries.getOrElse(Nil).zipWithIndex.map {
      case (entry, index) =>
        ListItem(
          name      = entry.companyName,
          changeUrl = AssetsMiniJourneysRoutes.UnquotedSharesCYAController.onPageLoad(index).url,
          removeUrl = AssetsMiniJourneysRoutes.UnquotedSharesConfirmRemovalController.onPageLoad(index).url
        )
    }
  }
}

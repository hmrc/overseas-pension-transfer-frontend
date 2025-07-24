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

import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import controllers.transferDetails.routes
import models.UserAnswers
import play.api.i18n.Messages
import queries.UnquotedShares
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.addtoalist.ListItem
import utils.AppUtils
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object QuotedSharesAmendContinueSummary extends AppUtils {

  def row(answers: UserAnswers)(implicit messages: Messages): SummaryListRow = {

    val count: Int = answers.get(UnquotedShares).getOrElse(Nil).size
    val valueText  = messages("unquotedSharesAmendContinue.summary.value", count)

    SummaryListRowViewModel(
      key     = "unquotedSharesAmendContinue.checkYourAnswersLabel",
      value   = ValueViewModel(valueText),
      actions = Seq(
        ActionItemViewModel("site.change", AssetsMiniJourneysRoutes.UnquotedSharesAmendContinueController.onPageLoad().url)
          .withVisuallyHiddenText(messages("unquotedSharesAmendContinue.change.hidden"))
      )
    )
  }

  def rows(answers: UserAnswers): Seq[ListItem] = {
    val maybeEntries = answers.get(UnquotedShares)
    maybeEntries.getOrElse(Nil).zipWithIndex.map {
      case (entry, index) =>
        ListItem(
          name      = entry.companyName,
          changeUrl = AssetsMiniJourneysRoutes.UnquotedShareCYAController.onPageLoad(index).url,
          removeUrl = AssetsMiniJourneysRoutes.UnquotedSharesConfirmRemovalController.onPageLoad(index).url
        )
    }
  }
}

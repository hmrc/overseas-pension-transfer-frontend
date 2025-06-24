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

import controllers.transferDetails.routes
import models.{ShareType, UserAnswers}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._
import utils.AppUtils
import queries.UnquotedShares
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.addtoalist.ListItem

object AdditionalUnquotedShareSummary extends AppUtils {

  def row(answers: UserAnswers)(implicit messages: Messages): SummaryListRow = {

    val count: Int = countShares(answers, ShareType.Unquoted)
    val valueText  = messages("additionalUnquotedShare.summary.value", count)

    SummaryListRowViewModel(
      key     = "additionalUnquotedShare.checkYourAnswersLabel",
      value   = ValueViewModel(valueText),
      actions = Seq(
        ActionItemViewModel("site.change", routes.AdditionalUnquotedShareController.onPageLoad().url)
          .withVisuallyHiddenText(messages("additionalUnquotedShare.change.hidden"))
      )
    )
  }

  def rows(answers: UserAnswers): Seq[ListItem] =
    answers.get(UnquotedShares).getOrElse(Nil).zipWithIndex.map {
      case (entry, index) =>
        ListItem(
          name      = entry.companyName,
          // changeUrl = CheckChildDetailsPage(Index(index)).changeLink(waypoints, sourcePage).url,
          changeUrl = routes.UnquotedShareCYAController.onPageLoad().url, // TODO should take index and change for that
          // removeUrl = routes.RemoveChildController.onPageLoad(waypoints, Index(index)).url
          removeUrl = routes.UnquotedSharesConfirmRemovalController.onPageLoad(index).url // TODO should remove the indexed one
        )
    }
}

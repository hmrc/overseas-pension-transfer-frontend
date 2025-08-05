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

package viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.otherAssets

import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import models.{CheckMode, UserAnswers}
import play.api.i18n.Messages
import queries.assets.OtherAssetsQuery
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.addtoalist.ListItem
import utils.AppUtils
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object OtherAssetsAmendContinueSummary extends AppUtils {

  def row(answers: UserAnswers)(implicit messages: Messages): SummaryListRow = {

    val count: Int = answers.get(OtherAssetsQuery).getOrElse(Nil).size
    val valueText  = messages("otherAssetsAmendContinue.summary.value", count)

    SummaryListRowViewModel(
      key     = "otherAssetsAmendContinue.checkYourAnswersLabel",
      value   = ValueViewModel(valueText),
      actions = Seq(
        ActionItemViewModel("site.change", AssetsMiniJourneysRoutes.OtherAssetsAmendContinueController.onPageLoad(mode = CheckMode).url)
          .withVisuallyHiddenText(messages("otherAssetsAmendContinue.change.hidden"))
      )
    )
  }

  def rows(answers: UserAnswers): Seq[ListItem] = {
    val maybeEntries = answers.get(OtherAssetsQuery)
    maybeEntries.getOrElse(Nil).zipWithIndex.map {
      case (entry, index) =>
        ListItem(
          name      = entry.assetDescription,
          changeUrl = AssetsMiniJourneysRoutes.OtherAssetsCYAController.onPageLoad(index).url,
          removeUrl = AssetsMiniJourneysRoutes.OtherAssetsConfirmRemovalController.onPageLoad(index).url
        )
    }
  }
}

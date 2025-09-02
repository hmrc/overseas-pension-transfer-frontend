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

package viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.property

import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import handlers.AssetThresholdHandler
import models.assets.TypeOfAsset
import models.{Mode, UserAnswers}
import play.api.i18n.Messages
import queries.assets.PropertyQuery
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.addtoalist.ListItem
import viewmodels.AddressViewModel
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object PropertyAmendContinueSummary {

  private val thresholdHandler = new AssetThresholdHandler()
  private val threshold        = 5

  def row(mode: Mode, userAnswers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {

    val maybeEntries = userAnswers.get(PropertyQuery)
    val count        = thresholdHandler.getAssetCount(userAnswers, TypeOfAsset.Property)
    val valueText    = messages("propertyAmendContinue.summary.value", maybeEntries.map(_.size).getOrElse(0))

    maybeEntries match {
      case Some(entries) if entries.nonEmpty =>
        val changeUrl =
          if (count < threshold) {
            AssetsMiniJourneysRoutes.PropertyAmendContinueController.onPageLoad(mode).url
          } else {
            controllers.transferDetails.assetsMiniJourneys.property.routes.MorePropertyDeclarationController
              .onPageLoad(mode)
              .url
          }

        Some(
          SummaryListRowViewModel(
            key     = "propertyAmendContinue.checkYourAnswersLabel",
            value   = ValueViewModel(valueText),
            actions = Seq(
              ActionItemViewModel("site.change", changeUrl)
                .withVisuallyHiddenText(messages("propertyAmendContinue.change.hidden"))
            )
          )
        )
      case _                                 => None
    }
  }

  def rows(answers: UserAnswers): Seq[ListItem] = {
    val maybeEntries = answers.get(PropertyQuery)

    maybeEntries.getOrElse(Nil).zipWithIndex.map {
      case (entry, index) => {
        ListItem(
          name      = AddressViewModel.formatAddressAsString(entry.propertyAddress),
          changeUrl = AssetsMiniJourneysRoutes.PropertyCYAController.onPageLoad(index).url,
          removeUrl = AssetsMiniJourneysRoutes.PropertyConfirmRemovalController.onPageLoad(index).url
        )
      }
    }
  }
}

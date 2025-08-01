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
import models.{CheckMode, UserAnswers}
import pages.transferDetails.assetsMiniJourneys.property.PropertyAddressPage
import play.api.i18n.Messages
import queries.assets.PropertyQuery
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.addtoalist.ListItem
import viewmodels.AddressViewModel
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object PropertyAddressSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): SummaryListRow = {

    val count: Int = answers.get(PropertyQuery).getOrElse(Nil).size
    val valueText  = messages("quotedSharesAmendContinue.summary.value", count)

    SummaryListRowViewModel(
      key     = "propertyAddress.checkYourAnswersLabel",
      value   = ValueViewModel(HtmlContent(valueText)),
      actions = Seq(
        ActionItemViewModel("site.change", AssetsMiniJourneysRoutes.PropertyAmendContinueController.onPageLoad(mode = CheckMode).url)
          .withVisuallyHiddenText(messages("propertyAddress.change.hidden"))
      )
    )

  }

  def rows(answers: UserAnswers): Seq[ListItem] = {
    val maybeEntries = answers.get(PropertyQuery)
    maybeEntries.getOrElse(Nil).zipWithIndex.map {
      case (entry, index) =>
        ListItem(
          name      = AddressViewModel.formatAddressWithLineBreaks(entry.propertyAddress, ukMode = false).toString(),
          changeUrl = AssetsMiniJourneysRoutes.PropertyCYAController.onPageLoad(index).url,
          removeUrl = AssetsMiniJourneysRoutes.PropertyConfirmRemovalController.onPageLoad(index).url
        )
    }
  }
}

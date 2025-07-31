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

import models.UserAnswers
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.unquotedShares._

case object PropertySummary {

  def rows(userAnswers: UserAnswers, index: Int)(implicit messages: Messages): Seq[SummaryListRow] = {
    val propertyAddress: Option[SummaryListRow]     = PropertyAddressSummary.row(userAnswers)
    val propertyDescription: Option[SummaryListRow] = PropertyDescriptionSummary.row(userAnswers)
    val propertyValue: Option[SummaryListRow]       = PropertyValueSummary.row(userAnswers)

    Seq(propertyAddress, propertyDescription, propertyValue).flatten
  }
}

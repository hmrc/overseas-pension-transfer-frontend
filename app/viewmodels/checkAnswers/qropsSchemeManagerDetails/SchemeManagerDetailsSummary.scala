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

package viewmodels.checkAnswers.qropsSchemeManagerDetails

import models.{Mode, UserAnswers}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

case object SchemeManagerDetailsSummary {

  def rows(mode: Mode, userAnswers: UserAnswers, showChangeLinks: Boolean = true)(implicit messages: Messages): Seq[SummaryListRow] = {
    val managersType: Option[SummaryListRow] = SchemeManagerTypeSummary.row(mode, userAnswers, showChangeLinks)

    val managersName: Option[SummaryListRow] = SchemeManagersNameSummary.row(mode, userAnswers, showChangeLinks)

    val orgName: Option[SummaryListRow]       = SchemeManagerOrganisationNameSummary.row(mode, userAnswers, showChangeLinks)
    val orgIndividual: Option[SummaryListRow] = SchemeManagerOrgIndividualNameSummary.row(mode, userAnswers, showChangeLinks)

    val managerAddress: Option[SummaryListRow]  = SchemeManagersAddressSummary.row(mode, userAnswers, showChangeLinks)
    val managersEmail: Option[SummaryListRow]   = SchemeManagersEmailSummary.row(mode, userAnswers, showChangeLinks)
    val managersContact: Option[SummaryListRow] = SchemeManagersContactSummary.row(mode, userAnswers, showChangeLinks)

    Seq(
      managersType,
      managersName,
      orgName,
      orgIndividual,
      managerAddress,
      managersEmail,
      managersContact
    ).flatten
  }
}

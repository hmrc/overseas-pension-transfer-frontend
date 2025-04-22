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

import models.UserAnswers
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

case object SchemeManagerDetailsSummary {

  def rows(userAnswers: UserAnswers)(implicit messages: Messages): Seq[SummaryListRow] = {
    val managersType: Option[SummaryListRow] = QROPSSchemeManagerTypeSummary.row(userAnswers)

    val managersName: Option[SummaryListRow] = SchemeManagersNameSummary.row(userAnswers)

    val orgName: Option[SummaryListRow]       = OrganisationNameSummary.row(userAnswers)
    val orgIndividual: Option[SummaryListRow] = OrgIndividualNameSummary.row(userAnswers)

    // TODO update when SchemeManagerAddress implemented
    // val managerAddress: Option[SummaryListRow]       = SchemeManagerAddress.row(userAnswers)

    val managersEmail: Option[SummaryListRow]   = SchemeManagersEmailSummary.row(userAnswers)
    val managersContact: Option[SummaryListRow] = SchemeManagersContactSummary.row(userAnswers)

    Seq(
      managersType,
      managersName,
      orgName,
      orgIndividual,
      // TODO uncomment when SchemeManagerAddress implemented
      // managerAddress,
      managersEmail,
      managersContact
    ).flatten
  }
}

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

import models.UserAnswers
import pages.transferDetails.IsTransferCashOnlyPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.otherAssets.OtherAssetsAmendContinueSummary
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.property.PropertyAmendContinueSummary
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.quotedShares.QuotedSharesAmendContinueSummary
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesAmendContinueSummary

case object TransferDetailsSummary {

  def rows(userAnswers: UserAnswers)(implicit messages: Messages): Seq[SummaryListRow] = {
    val overseasTransferAllowance: Option[SummaryListRow] = OverseasTransferAllowanceSummary.row(userAnswers)
    val amountOfTransfer: Option[SummaryListRow]          = AmountOfTransferSummary.row(userAnswers)
    val isTransferTaxable: Option[SummaryListRow]         = IsTransferTaxableSummary.row(userAnswers)
    val whyTransferIsTaxable: Option[SummaryListRow]      = WhyTransferIsTaxableSummary.row(userAnswers)
    val applicableTaxExclusions: Option[SummaryListRow]   = ApplicableTaxExclusionsSummary.row(userAnswers)
    val amountOfTaxDeducted: Option[SummaryListRow]       = AmountOfTaxDeductedSummary.row(userAnswers)
    val netTransferAmount: Option[SummaryListRow]         = NetTransferAmountSummary.row(userAnswers)
    val dateOfTransfer: Option[SummaryListRow]            = DateOfTransferSummary.row(userAnswers)
    val isTransferCashOnly: Option[SummaryListRow]        = IsTransferCashOnlySummary.row(userAnswers)

    val showCashAmount       = userAnswers.get(IsTransferCashOnlyPage).contains(false)
    val cashAmountInTransfer =
      if (showCashAmount) CashAmountInTransferSummary.row(userAnswers) else None

    val totalUnquotedSharesRow: Option[SummaryListRow] = UnquotedSharesAmendContinueSummary.row(userAnswers)
    val totalQuotedSharesRow: Option[SummaryListRow]   = QuotedSharesAmendContinueSummary.row(userAnswers)
    val totalPropertiesRow: Option[SummaryListRow]     = PropertyAmendContinueSummary.row(userAnswers)
    val totalOtherAssetsRow: Option[SummaryListRow]    = OtherAssetsAmendContinueSummary.row(userAnswers)

    Seq(
      overseasTransferAllowance,
      amountOfTransfer,
      isTransferTaxable,
      whyTransferIsTaxable,
      applicableTaxExclusions,
      amountOfTaxDeducted,
      netTransferAmount,
      dateOfTransfer,
      isTransferCashOnly,
      cashAmountInTransfer,
      totalUnquotedSharesRow,
      totalQuotedSharesRow,
      totalPropertiesRow,
      totalOtherAssetsRow
    ).flatten
  }
}

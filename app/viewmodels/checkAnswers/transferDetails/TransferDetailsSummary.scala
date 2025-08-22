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

import models.{Mode, UserAnswers}
import pages.transferDetails.IsTransferCashOnlyPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.otherAssets.OtherAssetsAmendContinueSummary
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.property.PropertyAmendContinueSummary
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.quotedShares.QuotedSharesAmendContinueSummary
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesAmendContinueSummary

case object TransferDetailsSummary {

  def rows(mode: Mode, userAnswers: UserAnswers)(implicit messages: Messages): Seq[SummaryListRow] = {
    val overseasTransferAllowance: Option[SummaryListRow] = OverseasTransferAllowanceSummary.row(mode, userAnswers)
    val amountOfTransfer: Option[SummaryListRow]          = AmountOfTransferSummary.row(mode, userAnswers)
    val isTransferTaxable: Option[SummaryListRow]         = IsTransferTaxableSummary.row(mode, userAnswers)
    val whyTransferIsTaxable: Option[SummaryListRow]      = WhyTransferIsTaxableSummary.row(mode, userAnswers)
    val applicableTaxExclusions: Option[SummaryListRow]   = ApplicableTaxExclusionsSummary.row(mode, userAnswers)
    val amountOfTaxDeducted: Option[SummaryListRow]       = AmountOfTaxDeductedSummary.row(mode, userAnswers)
    val netTransferAmount: Option[SummaryListRow]         = NetTransferAmountSummary.row(mode, userAnswers)
    val dateOfTransfer: Option[SummaryListRow]            = DateOfTransferSummary.row(mode, userAnswers)
    val isTransferCashOnly: Option[SummaryListRow]        = IsTransferCashOnlySummary.row(mode, userAnswers)
    val typeOfAsset: Option[SummaryListRow]               = TypeOfAssetSummary.row(mode, userAnswers)

    val showCashAmount       = userAnswers.get(IsTransferCashOnlyPage).contains(false)
    val cashAmountInTransfer =
      if (showCashAmount) CashAmountInTransferSummary.row(mode, userAnswers) else None

    val totalUnquotedSharesRow: Option[SummaryListRow] = UnquotedSharesAmendContinueSummary.row(mode, userAnswers)
    val totalQuotedSharesRow: Option[SummaryListRow]   = QuotedSharesAmendContinueSummary.row(mode, userAnswers)
    val totalPropertiesRow: Option[SummaryListRow]     = PropertyAmendContinueSummary.row(mode, userAnswers)
    val totalOtherAssetsRow: Option[SummaryListRow]    = OtherAssetsAmendContinueSummary.row(mode, userAnswers)

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
      typeOfAsset,
      cashAmountInTransfer,
      totalUnquotedSharesRow,
      totalQuotedSharesRow,
      totalPropertiesRow,
      totalOtherAssetsRow
    ).flatten
  }
}

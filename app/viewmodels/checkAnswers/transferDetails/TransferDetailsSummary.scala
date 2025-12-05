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
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.otherAssets.OtherAssetsAmendContinueSummary
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.property.PropertyAmendContinueSummary
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.quotedShares.QuotedSharesAmendContinueSummary
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesAmendContinueSummary

case object TransferDetailsSummary {

  def rows(mode: Mode, userAnswers: UserAnswers, showChangeLinks: Boolean = true)(implicit messages: Messages): Seq[SummaryListRow] = {
    val overseasTransferAllowance: Option[SummaryListRow] = OverseasTransferAllowanceSummary.row(mode, userAnswers, showChangeLinks)
    val amountOfTransfer: Option[SummaryListRow]          = AmountOfTransferSummary.row(mode, userAnswers, showChangeLinks)
    val isTransferTaxable: Option[SummaryListRow]         = IsTransferTaxableSummary.row(mode, userAnswers, showChangeLinks)
    val whyTransferIsTaxable: Option[SummaryListRow]      = WhyTransferIsTaxableSummary.row(mode, userAnswers, showChangeLinks)
    val whyTransferIsNotTaxable: Option[SummaryListRow]   = WhyTransferIsNotTaxableSummary.row(mode, userAnswers, showChangeLinks)
    val applicableTaxExclusions: Option[SummaryListRow]   = ApplicableTaxExclusionsSummary.row(mode, userAnswers, showChangeLinks)
    val amountOfTaxDeducted: Option[SummaryListRow]       = AmountOfTaxDeductedSummary.row(mode, userAnswers, showChangeLinks)
    val netTransferAmount: Option[SummaryListRow]         = NetTransferAmountSummary.row(mode, userAnswers, showChangeLinks)
    val dateOfTransfer: Option[SummaryListRow]            = DateOfTransferSummary.row(mode, userAnswers, showChangeLinks)
    val isTransferCashOnly: Option[SummaryListRow]        = IsTransferCashOnlySummary.row(mode, userAnswers, showChangeLinks)
    val typeOfAsset: Option[SummaryListRow]               = TypeOfAssetSummary.row(mode, userAnswers, showChangeLinks)

    val showCashAmount       = userAnswers.get(IsTransferCashOnlyPage).contains(false)
    val cashAmountInTransfer =
      if (showCashAmount) CashAmountInTransferSummary.row(mode, userAnswers, showChangeLinks) else None

    val totalUnquotedSharesRow: Option[SummaryListRow] = UnquotedSharesAmendContinueSummary.row(mode, userAnswers, showChangeLinks)
    val moreThanFiveUnquotedSharesRow                  = UnquotedSharesAmendContinueSummary.moreThanFiveUnquotedSharesRow(mode, userAnswers, showChangeLinks)
    val totalQuotedSharesRow: Option[SummaryListRow]   = QuotedSharesAmendContinueSummary.row(mode, userAnswers, showChangeLinks)
    val moreThanFiveQuotedSharesRow                    = QuotedSharesAmendContinueSummary.moreThanFiveQuotedSharesRow(mode, userAnswers, showChangeLinks)
    val totalPropertiesRow: Option[SummaryListRow]     = PropertyAmendContinueSummary.row(mode, userAnswers, showChangeLinks)
    val moreThanFivePropertiesRow                      = PropertyAmendContinueSummary.moreThanFivePropertiesRow(mode, userAnswers, showChangeLinks)
    val totalOtherAssetsRow: Option[SummaryListRow]    = OtherAssetsAmendContinueSummary.row(mode, userAnswers, showChangeLinks)
    val moreThanFiveOtherAssetsRow                     = OtherAssetsAmendContinueSummary.moreThanFiveOtherAssetsRow(mode, userAnswers, showChangeLinks)

    Seq(
      overseasTransferAllowance,
      amountOfTransfer,
      isTransferTaxable,
      whyTransferIsTaxable,
      whyTransferIsNotTaxable,
      applicableTaxExclusions,
      amountOfTaxDeducted,
      netTransferAmount,
      dateOfTransfer,
      isTransferCashOnly,
      typeOfAsset,
      cashAmountInTransfer,
      totalUnquotedSharesRow,
      moreThanFiveUnquotedSharesRow,
      totalQuotedSharesRow,
      moreThanFiveQuotedSharesRow,
      totalPropertiesRow,
      moreThanFivePropertiesRow,
      totalOtherAssetsRow,
      moreThanFiveOtherAssetsRow
    ).flatten
  }
}

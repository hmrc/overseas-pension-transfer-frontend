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

import base.SpecBase
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import models.CheckMode
import org.scalatest.freespec.AnyFreeSpec
import pages.transferDetails.assetsMiniJourneys.cash.CashAmountInTransferPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text

class CashAmountInTransferSummarySpec extends AnyFreeSpec with SpecBase {

  "CashAmountInTransferSummary" - {
    implicit val messages: Messages = stubMessages()

    "must return a SummaryListRow when CashAmountInTransferPage has a value" in {
      val answers = emptyUserAnswers.set(CashAmountInTransferPage, BigDecimal(12345.33)).success.value
      val result  = CashAmountInTransferSummary.row(CheckMode, answers)

      result mustBe defined
      result.get.key.content mustBe Text(messages("cashAmountInTransfer.checkYourAnswersLabel"))
      result.get.value.content mustBe Text("£12,345.33")
      result.get.actions.get.items.head.href mustBe
        AssetsMiniJourneysRoutes.CashAmountInTransferController.onPageLoad(CheckMode).url
    }
  }
}

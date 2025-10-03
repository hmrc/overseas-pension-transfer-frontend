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

import models.{Mode, SessionData, UserAnswers}
import pages.transferDetails.assetsMiniJourneys.property.PropertyAddressPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.AddressViewModel
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object PropertyAddressSummary {

  def row(mode: Mode, userAnswers: UserAnswers, index: Int)(implicit messages: Messages): Option[SummaryListRow] = {
    userAnswers.get(PropertyAddressPage(index)).map {
      address =>
        {
          val addressVM = AddressViewModel.formatAddressWithLineBreaks(address, ukMode = false)
          SummaryListRowViewModel(
            key     = "propertyAddress.checkYourAnswersLabel",
            value   = ValueViewModel(HtmlContent(addressVM)),
            actions = Seq(
              ActionItemViewModel("site.change", PropertyAddressPage(index).changeLink(mode).url)
                .withVisuallyHiddenText(messages("propertyAddress.change.hidden"))
            )
          )
        }
    }
  }
}

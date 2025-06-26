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

import controllers.transferDetails.routes
import models.{CheckMode, UserAnswers}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._
import utils.AppUtils
import models.ShareType
import pages.transferDetails.UnquotedShareCompanyNamePage
import models.{ShareEntry, ShareType}
import play.api.libs.json._
//import play.api.routing.Router.empty.routes
import play.twirl.api.HtmlFormat
import services.TransferDetailsService

object AdditionalUnquotedShareSummary extends AppUtils {

  def row(answers: UserAnswers)(implicit messages: Messages): SummaryListRow = {

    val count: Int = countShares(answers, ShareType.Unquoted)
    val valueText  = messages("additionalUnquotedShare.summary.value", count)

    SummaryListRowViewModel(
      key     = "additionalUnquotedShare.checkYourAnswersLabel",
      value   = ValueViewModel(valueText),
      actions = Seq(
        ActionItemViewModel("site.change", routes.AdditionalUnquotedShareController.onPageLoad(CheckMode).url)
          .withVisuallyHiddenText(messages("additionalUnquotedShare.change.hidden"))
      )
    )
  }
}
/*
  def rows(userAnswers: UserAnswers, transferDetailsService: TransferDetailsService)(implicit messages: Messages): Seq[SummaryListRow] = {
    val path = sharesPathForType(ShareType.Unquoted)

    userAnswers.data
      .validate(path.read[List[ShareEntry]])
      .getOrElse(Nil)
      .filter(_.companyName.trim != "")
      .zipWithIndex
      .map { case (entry, index) =>
        SummaryListRowViewModel(
          key   = "SomeKey",
          value = ValueViewModel(HtmlFormat.escape(entry.companyName).toString)
        )
      }
  }
  def rows(userAnswers: UserAnswers, transferDetailsService: TransferDetailsService)(implicit messages: Messages): Seq[SummaryListRow] = {
    val path = sharesPathForType(ShareType.Unquoted)

    userAnswers.data
      .validate(path.read[List[ShareEntry]])
      .getOrElse(Nil)
      .zipWithIndex
      .flatMap { case (entry, index) =>
        // println(s"[rows] Index: $index, CompanyName: ${entry.companyName}")
        row(entry, index)
      }
  }

  def row(entry: ShareEntry, index: Int)(implicit messages: Messages): Option[SummaryListRow] =
    Option(entry.companyName)
      .map { validName =>
        SummaryListRowViewModel(
          value   = ValueViewModel(HtmlFormat.escape(validName.trim).toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.UnquotedShareCYAController.onPageLoad().url)
              .withVisuallyHiddenText(messages("unquotedShareCompanyName.change.hidden")),
            ActionItemViewModel("site.remove", routes.UnquotedSharesConfirmRemovalController.onPageLoad().url)
              .withVisuallyHiddenText(messages("unquotedShareCompanyName.remove.hidden"))
          )
        )
      }
}*/
/*,
         actions = Seq(
           ActionItemViewModel("site.change", routes.UnquotedShareCYAController.onPageLoad().url)
             .withVisuallyHiddenText(messages("unquotedShareCompanyName.change.hidden")),
           ActionItemViewModel("site.remove", routes.UnquotedSharesConfirmRemovalController.onPageLoad().url)
             .withVisuallyHiddenText(messages("unquotedShareCompanyName.remove.hidden"))
         )*/

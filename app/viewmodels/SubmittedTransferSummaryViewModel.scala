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

package viewmodels

import controllers.viewandamend.routes
import models.QtStatus.{AmendInProgress, Submitted}
import models.UserAnswers
import play.api.i18n.Messages
import play.twirl.api.Html
import utils.DateTimeFormats.localDateTimeFormatter

import java.time.{Instant, LocalDateTime, ZoneId}

case object SubmittedTransferSummaryViewModel {

  def rows(maybeDraft: Option[UserAnswers], answers: List[UserAnswers], versionNumber: String)(implicit messages: Messages): Html = {
    def changeLinkText(isDraftDefined: Boolean) =
      if (isDraftDefined) messages("submittedTransferSummary.view") else messages("submittedTransferSummary.viewOrAmend")
    def changeLinkHref(isDraftDefined: Boolean) =
      if (isDraftDefined) {
        routes.ViewAmendSubmittedController.view(answers.head.id, answers.head.pstr, Submitted, versionNumber).url
      } else routes.ViewAmendSelectorController.onPageLoad(answers.head.id, answers.head.pstr, Submitted, versionNumber).url

    val versions = versionNumber.toInt to 1 by -1

    val draftTableRow = maybeDraft.fold("") {
      draft =>
        buildRow(
          draft.lastUpdated,
          messages("submittedTransferSummary.draft"),
          routes.ViewAmendSubmittedController.fromDraft(draft.id, draft.pstr, AmendInProgress, versionNumber).url,
          messages("submittedTransferSummary.reviewAndSubmit")
        )
    }

    val mostRecentSubmittedVersion = {
      val answer = answers.head
      buildRow(answer.lastUpdated, versionNumber.toInt.toString, changeLinkHref(maybeDraft.isDefined), changeLinkText(maybeDraft.isDefined))
    }

    val submittedRecords = answers.tail.zip(versions.tail).map {
      case (answer, version) =>
        val stringifyVersion = version.toString.length match {
          case 1 => s"00$version"
          case 2 => s"0$version"
          case _ => version.toString
        }

        buildRow(
          answer.lastUpdated,
          version.toString,
          routes.ViewAmendSubmittedController.view(answers.head.id, answers.head.pstr, Submitted, stringifyVersion).url,
          messages("submittedTransferSummary.view")
        )
    }.mkString

    Html(draftTableRow + mostRecentSubmittedVersion + submittedRecords)
  }

  private def buildRow(date: Instant, version: String, href: String, linkText: String): String =
    s""" <tr class="govuk-table__row">
       |            <th scope="row" class="govuk-table__header">$version</th>
       |            <td class="govuk-table__cell">${LocalDateTime.ofInstant(date, ZoneId.systemDefault()).format(localDateTimeFormatter)}</td>
       |            <td class="govuk-table__cell"><a href=$href class="govuk-link">$linkText</a></td>
       |        </tr>""".stripMargin

}

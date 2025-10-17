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

import models.QtStatus.Submitted
import models.UserAnswers
import play.twirl.api.Html
import utils.DateTimeFormats.localDateTimeFormatter

import java.time.{Instant, LocalDateTime, ZoneId}

case object SubmittedTransferSummaryViewModel {

  def rows(maybeDraft: Option[UserAnswers], answers: List[UserAnswers], versionNumber: String): Html = {
    def changeLinkText(isDraftDefined: Boolean) = if (isDraftDefined) "View" else "View or amend"
    def changeLinkHref(isDraftDefined: Boolean) =
      if (isDraftDefined) {
        controllers.routes.ViewSubmittedController.fromDashboard(answers.head.id, answers.head.pstr, Submitted, versionNumber).url
      } else ""

    val versions = versionNumber.toInt to 1 by -1

    val draftTableRow = maybeDraft.fold("") {
      draft => buildRow(draft.lastUpdated, "Draft", "", "Review and submit")
    }

    val mostRecentSubmittedVersion = {
      val answer = answers.head
      buildRow(answer.lastUpdated, versionNumber.toInt.toString, changeLinkHref(maybeDraft.isDefined), changeLinkText(maybeDraft.isDefined))
    }

    val submittedRecords = answers.tail.zip(versions.tail).map {
      case (answer, version) =>
        buildRow(
          answer.lastUpdated,
          version.toString,
          controllers.routes.ViewSubmittedController.fromDashboard(answers.head.id, answers.head.pstr, Submitted, versionNumber).url,
          "View"
        )
    }.mkString

    Html(draftTableRow + mostRecentSubmittedVersion + submittedRecords)
  }

  def buildRow(date: Instant, version: String, href: String, linkText: String) =
    s""" <tr class="govuk-table__row">
       |            <th scope="row" class="govuk-table__header">$version</th>
       |            <td class="govuk-table__cell">${LocalDateTime.ofInstant(date, ZoneId.systemDefault()).format(localDateTimeFormatter)}</td>
       |            <td class="govuk-table__cell"><a href=$href class="govuk-link">$linkText</a></td>
       |        </tr>""".stripMargin

}

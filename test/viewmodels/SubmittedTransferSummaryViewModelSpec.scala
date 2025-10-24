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

import base.SpecBase
import org.scalatest.freespec.AnyFreeSpec
import play.api.i18n.Messages
import play.twirl.api.Html
import utils.DateTimeFormats.localDateTimeFormatter

import java.time.{LocalDateTime, ZoneId}

class SubmittedTransferSummaryViewModelSpec extends AnyFreeSpec with SpecBase {

  implicit val messages: Messages = messages(applicationBuilder().build())
  val userAnswers                 = userAnswersMemberName.copy(id = testQtNumber)

  ".rows" - {
    "return a row with View or amend changeLink as the first row when draft is None" in {
      SubmittedTransferSummaryViewModel.rows(None, List(userAnswers.copy(lastUpdated = now)), "001") mustBe
        Html(s""" <tr class="govuk-table__row">
                |            <th scope="row" class="govuk-table__header">1</th>
                |            <td class="govuk-table__cell">${LocalDateTime.ofInstant(now, ZoneId.systemDefault()).format(localDateTimeFormatter)}</td>
                |            <td class="govuk-table__cell"><a href=/report-transfer-qualified-recognised-overseas-pension-scheme/view-amend?qtReference=QT123456&pstr=12345678AB&qtStatus=Submitted&versionNumber=001 class="govuk-link">View or amend</a></td>
                |        </tr>""".stripMargin)
    }

    "return 2 rows with draft and View changeLink as the first row when draft is defined" in {
      SubmittedTransferSummaryViewModel.rows(Some(userAnswers.copy(lastUpdated = now)), List(userAnswers.copy(lastUpdated = now)), "001") mustBe
        Html(s""" <tr class="govuk-table__row">
                |            <th scope="row" class="govuk-table__header">Draft</th>
                |            <td class="govuk-table__cell">${LocalDateTime.ofInstant(now, ZoneId.systemDefault()).format(localDateTimeFormatter)}</td>
                |            <td class="govuk-table__cell"><a href=/report-transfer-qualified-recognised-overseas-pension-scheme/amend-in-progress-draft?qtReference=QT123456&pstr=12345678AB&qtStatus=AmendInProgress&versionNumber=001 class="govuk-link">Review and submit</a></td>
                |        </tr> <tr class="govuk-table__row">
                |            <th scope="row" class="govuk-table__header">1</th>
                |            <td class="govuk-table__cell">${LocalDateTime.ofInstant(now, ZoneId.systemDefault()).format(localDateTimeFormatter)}</td>
                |            <td class="govuk-table__cell"><a href=/report-transfer-qualified-recognised-overseas-pension-scheme/view-submitted-transfer?qtReference=${testQtNumber.value}&pstr=12345678AB&qtStatus=Submitted&versionNumber=001 class="govuk-link">View</a></td>
                |        </tr>""".stripMargin)
    }

    "return 2 rows with View or amend changeLink as first item and View changeLink when version is greater than 1 draft is None" in {
      SubmittedTransferSummaryViewModel.rows(
        None,
        List(userAnswers.copy(lastUpdated = now), userAnswers.copy(lastUpdated = now)),
        "002"
      ) mustBe
        Html(s""" <tr class="govuk-table__row">
                |            <th scope="row" class="govuk-table__header">2</th>
                |            <td class="govuk-table__cell">${LocalDateTime.ofInstant(now, ZoneId.systemDefault()).format(localDateTimeFormatter)}</td>
                |            <td class="govuk-table__cell"><a href=/report-transfer-qualified-recognised-overseas-pension-scheme/view-amend?qtReference=QT123456&pstr=12345678AB&qtStatus=Submitted&versionNumber=002 class="govuk-link">View or amend</a></td>
                |        </tr> <tr class="govuk-table__row">
                |            <th scope="row" class="govuk-table__header">1</th>
                |            <td class="govuk-table__cell">${LocalDateTime.ofInstant(now, ZoneId.systemDefault()).format(localDateTimeFormatter)}</td>
                |            <td class="govuk-table__cell"><a href=/report-transfer-qualified-recognised-overseas-pension-scheme/view-submitted-transfer?qtReference=${testQtNumber.value}&pstr=12345678AB&qtStatus=Submitted&versionNumber=001 class="govuk-link">View</a></td>
                |        </tr>""".stripMargin)
    }
  }

}

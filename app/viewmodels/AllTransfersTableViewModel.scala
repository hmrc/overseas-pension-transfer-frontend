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

import config.FrontendAppConfig
import models.QtStatus.{AmendInProgress, Compiled, InProgress, Submitted}
import models.{AllTransfersItem, TransferReportQueryParams}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Content
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, Table, TableRow}

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneOffset}

final case class AllTransfersTableViewModel(table: Table)

object AllTransfersTableViewModel {

  private val dateFmt = DateTimeFormatter.ofPattern("d MMMM uuuu")
  private val timeFmt = DateTimeFormatter.ofPattern("h:mma")

  private def dashIfEmpty(s: String): String =
    if (s.trim.isEmpty) "-" else s

  private def memberName(first: Option[String], last: Option[String]): String =
    dashIfEmpty(Seq(first.getOrElse(""), last.getOrElse("")).map(_.trim).filter(_.nonEmpty).mkString(" "))

  private val rowPad                 = "govuk-!-padding-bottom-5"
  private def cell(content: Content) = TableRow(content = content, classes = rowPad)

  private def updatedCell(oi: Option[Instant]): Content =
    oi match {
      case Some(i) =>
        val zdt     = i.atZone(ZoneOffset.UTC)
        val dateStr = dateFmt.format(zdt)
        val timeStr = timeFmt.format(zdt).toLowerCase
        val html    = HtmlFormat.raw(
          s"""<p class="govuk-body govuk-!-margin-bottom-0">$dateStr</p><p class="govuk-body-s govuk-!-margin-bottom-0">$timeStr</p>"""
        )
        HtmlContent(html)
      case None    => Text("-")
    }

  def from(items: Seq[AllTransfersItem], currentPage: Int)(implicit messages: Messages, appConfig: FrontendAppConfig): Table = {
    val head: Seq[HeadCell] = Seq(
      HeadCell(Text(messages("dashboard.allTransfers.head.member"))),
      HeadCell(Text(messages("dashboard.allTransfers.head.status"))),
      HeadCell(Text(messages("dashboard.allTransfers.head.reference"))),
      HeadCell(Text(messages("dashboard.allTransfers.head.updated")))
    )

    val rows: Seq[Seq[TableRow]] = items.map { it =>
      val name = memberName(it.memberFirstName, it.memberSurname)
      val stat = it.qtStatus.map {
        case Compiled | Submitted => messages("dashboard.allTransfers.status.submitted")
        case InProgress           => messages("dashboard.allTransfers.status.inProgress")
        case AmendInProgress      => messages("dashboard.allTransfers.status.amendInProgress")
      }.getOrElse("-")
      val ref  = it.transferId

      val params = TransferReportQueryParams(
        transferId    = Some(ref),
        qtStatus      = it.qtStatus,
        pstr          = it.pstrNumber,
        versionNumber = it.qtVersion,
        memberName    = name,
        currentPage   = currentPage
      )

      val linkHtml = HtmlFormat.raw(s"""<a href="${TransferReportQueryParams.toUrl(params)}" class="govuk-link">$name</a>""")
      val refText  = if (params.qtStatus.contains(InProgress)) {
        Text(messages("dashboard.allTransfers.reference.inProgressText"))
      } else {
        Text(ref.value)
      }

      Seq(
        cell(content = HtmlContent(linkHtml)),
        cell(content = Text(stat)),
        cell(content = refText),
        cell(content = updatedCell(it.lastUpdatedDate))
      )
    }

    val hideCaption = if (appConfig.allowDashboardSearch) "govuk-visually-hidden" else ""

    Table(
      head           = Some(head),
      rows           = rows,
      caption        = Some(messages("dashboard.search.results.heading")),
      captionClasses = s"govuk-table__caption--m $hideCaption",
      attributes     = if (appConfig.allowDashboardSearch) Map("aria-hidden" -> "true") else Map.empty
    )
  }
}

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

import models.QtStatus.{Compiled, InProgress, Submitted}
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

  def from(items: Seq[AllTransfersItem], currentPage: Int)(implicit messages: Messages): Table = {
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
      }.getOrElse("-")
      val ref  = it.qtReference.map(_.value).getOrElse("-")

      val params = TransferReportQueryParams(
        transferReference = it.transferReference,
        qtReference       = it.qtReference.map(_.value),
        qtStatus          = it.qtStatus,
        pstr              = it.pstrNumber,
        versionNumber     = it.qtVersion,
        memberName        = name,
        currentPage       = currentPage
      )

      val linkHtml = HtmlFormat.raw(s"""<a href="${TransferReportQueryParams.toUrl(params)}" class="govuk-link">$name</a>""")

      Seq(
        cell(content = HtmlContent(linkHtml)),
        cell(content = Text(stat)),
        cell(content = Text(ref)),
        cell(content = updatedCell(it.lastUpdatedDate))
      )
    }

    Table(
      head = Some(head),
      rows = rows
    )
  }
}

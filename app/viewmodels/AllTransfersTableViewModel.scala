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

import models.AllTransfersItem
import models.QtStatus.{Compiled, InProgress, Submitted}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Content
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, Table, TableRow}

import java.time.LocalDate
import java.time.format.DateTimeFormatter

final case class AllTransfersTableViewModel(table: Table)

object AllTransfersTableViewModel {

  private val dateFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM uuuu")

  private def dashIfEmpty(s: String): String =
    if (s.trim.isEmpty) "-" else s

  private def fmtOpt[A](oa: Option[A])(f: A => String): String =
    oa.map(f).filter(_.nonEmpty).getOrElse("-")

  private def memberName(first: Option[String], last: Option[String]): String =
    dashIfEmpty(Seq(first.getOrElse(""), last.getOrElse("")).map(_.trim).filter(_.nonEmpty).mkString(" "))

  private def fmtLocalDate(od: Option[LocalDate]): String =
    fmtOpt(od)(d => dateFmt.format(d))

  private val rowPad = "govuk-!-padding-bottom-5"

  private def cell(content: Content) =
    TableRow(content = content, classes = rowPad)

  def from(items: Seq[AllTransfersItem])(implicit messages: Messages): Table = {

    val head: Seq[HeadCell] = Seq(
      HeadCell(Text(messages("dashboard.allTransfers.head.member"))),
      HeadCell(Text(messages("dashboard.allTransfers.head.status"))),
      HeadCell(Text(messages("dashboard.allTransfers.head.reference"))),
      HeadCell(Text(messages("dashboard.allTransfers.head.updated")))
    )

    val rows: Seq[Seq[TableRow]] = items.map { it =>
      val name = memberName(it.memberFirstName, it.memberSurname)
      val link = HtmlFormat.raw(s"""<a class="govuk-link" href="#">$name</a>""") // TODO: Replace with actual link
      val stat = it.qtStatus.map {
        case Compiled | Submitted => messages("dashboard.allTransfers.status.submitted")
        case InProgress           => messages("dashboard.allTransfers.status.inProgress")
      }.getOrElse("-")
      val ref  = it.qtReference.map(_.value).getOrElse("-")
      val upd  = fmtLocalDate(it.lastUpdatedDate)

      Seq(
        cell(content = HtmlContent(link)),
        cell(content = Text(stat)),
        cell(content = Text(ref)),
        cell(content = Text(upd))
      )
    }

    Table(
      head = Some(head),
      rows = rows
    )
  }
}

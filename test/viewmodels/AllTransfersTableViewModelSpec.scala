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
import models.{AllTransfersItem, QtStatus}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, TableRow}

import java.time.{Instant, LocalDate, ZoneOffset}
import java.time.format.DateTimeFormatter

class AllTransfersTableViewModelSpec extends AnyFreeSpec with SpecBase with Matchers {

  implicit val messages: Messages = stubMessagesApi().preferred(Seq.empty)

  private def toInstant(d: LocalDate): Instant =
    d.atStartOfDay(ZoneOffset.UTC).toInstant

  private def textOfHead(h: HeadCell): String = h.content match {
    case Text(s) => s
    case other   => fail(s"Expected Text head content, got: $other")
  }

  private def htmlOf(row: TableRow): String =
    row.content match {
      case HtmlContent(h) => h.toString
      case Text(s)        => s
      case other          => fail(s"Unexpected content in TableRow: $other")
    }

  "AllTransfersTableViewModel.from" - {

    "renders headers, a member link, submitted status label, reference, and formatted submission date" in {
      val submitted = AllTransfersItem(
        transferReference = Some("TR-001"),
        qtReference       = None,
        qtVersion         = None,
        nino              = None,
        memberFirstName   = Some("Ada"),
        memberSurname     = Some("Lovelace"),
        submissionDate    = Some(toInstant(LocalDate.of(2025, 9, 24))),
        lastUpdated       = None,
        qtStatus          = Some(QtStatus.Submitted),
        pstrNumber        = None,
        qtDate            = None
      )

      val table = AllTransfersTableViewModel.from(Seq(submitted))

      val heads = table.head.value
      heads.map(textOfHead) mustBe Seq(
        "dashboard.allTransfers.head.member",
        "dashboard.allTransfers.head.status",
        "dashboard.allTransfers.head.reference",
        "dashboard.allTransfers.head.updated"
      )

      val row = table.rows.head
      row must have length 4

      all(row.map(_.classes)) must include("govuk-!-padding-bottom-5")

      val memberHtml = htmlOf(row.head)
      memberHtml must include("""<a class="govuk-link""")
      memberHtml must include("Ada Lovelace")

      htmlOf(row(1)) mustBe "dashboard.allTransfers.status.submitted"
      htmlOf(row(2)) mustBe "-"
      htmlOf(row(3)) mustBe "24 September 2025"
    }

    "renders in-progress status and uses lastUpdated when present" in {
      val inProgress = AllTransfersItem(
        transferReference = None,
        qtReference       = None,
        qtVersion         = None,
        nino              = None,
        memberFirstName   = Some("  "),
        memberSurname     = Some(""),
        submissionDate    = None,
        lastUpdated       = Some(toInstant(LocalDate.of(2025, 1, 5))),
        qtStatus          = Some(QtStatus.InProgress),
        pstrNumber        = None,
        qtDate            = None
      )

      val table = AllTransfersTableViewModel.from(Seq(inProgress))
      val row   = table.rows.head

      htmlOf(row.head) must include(">-</a>")
      htmlOf(row(1)) mustBe "dashboard.allTransfers.status.inProgress"
      htmlOf(row(2)) mustBe "-"
      htmlOf(row(3)) mustBe "5 January 2025"
    }

    "maps Compiled status to submitted label (same as Submitted)" in {
      val compiled = AllTransfersItem(
        transferReference = None,
        qtReference       = None,
        qtVersion         = None,
        nino              = None,
        memberFirstName   = Some("Jean"),
        memberSurname     = Some("Jarvis"),
        submissionDate    = Some(toInstant(LocalDate.of(2024, 12, 31))),
        lastUpdated       = None,
        qtStatus          = Some(QtStatus.Compiled),
        pstrNumber        = None,
        qtDate            = None
      )

      val table = AllTransfersTableViewModel.from(Seq(compiled))
      val row   = table.rows.head

      htmlOf(row(1)) mustBe "dashboard.allTransfers.status.submitted"
      htmlOf(row(3)) mustBe "31 December 2024"
    }
  }
}

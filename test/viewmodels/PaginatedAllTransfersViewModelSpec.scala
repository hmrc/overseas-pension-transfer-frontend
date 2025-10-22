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
import models.AllTransfersItem
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneOffset, ZonedDateTime}

class PaginatedAllTransfersViewModelSpec extends AnyFreeSpec with SpecBase with Matchers {

  implicit val messages: Messages = stubMessagesApi().preferred(Seq.empty)

  private def mkItem(idx: Int, when: Instant): AllTransfersItem =
    AllTransfersItem(
      transferId      = userAnswersTransferNumber,
      qtVersion       = None,
      nino            = None,
      memberFirstName = Some(s"Name$idx"),
      memberSurname   = Some("McUser"),
      submissionDate  = None,
      lastUpdated     = Some(when),
      qtStatus        = None,
      pstrNumber      = None,
      qtDate          = None
    )

  private def utc(y: Int, m: Int, d: Int, hh: Int = 0, mm: Int = 0): Instant =
    ZonedDateTime.of(y, m, d, hh, mm, 0, 0, ZoneOffset.UTC).toInstant

  private val dateFmt = DateTimeFormatter.ofPattern("d MMMM uuuu")
  private val timeFmt = DateTimeFormatter.ofPattern("h:mma")

  // This regex extracts the time and date from the paragraph structure of the lastUpdated call
  // capture group 1 == date, capture group 2 == time
  private val dateTimeR =
    """(?is)<p[^>]*>\s*([^<]+?)\s*</p>\s*<p[^>]*>\s*([^<]+?)\s*</p>""".r

  private def lastUpdatedDateTime(row: TableRow): (String, String) = row.content match {
    case HtmlContent(h) =>
      dateTimeR.findFirstMatchIn(h.toString) match {
        case Some(m) => (m.group(1), m.group(2))
        case None    => fail(s"Could not parse updated cell HTML: ${h.toString.take(200)}")
      }
    case Text("-")      => ("-", "-")
    case other          => fail(s"Unexpected updated cell content: $other")
  }

  private def getLastUpdatedCol(vm: PaginatedAllTransfersViewModel): Seq[(String, String)] =
    vm.table.rows.map(_.last).map(lastUpdatedDateTime)

  private def fmt(i: Instant): (String, String) = {
    val z = i.atZone(ZoneOffset.UTC)
    (dateFmt.format(z), timeFmt.format(z).toLowerCase)
  }

  private def urlFor(n: Int): String = s"/dash?page=$n"

  "PaginatedAllTransfersViewModel.build" - {

    "must return no pagination when there is only a single page" in {
      val items    = (1 to 5).map(i => mkItem(i, utc(2025, 9, 24, 10, 15)))
      val pageSize = 10

      val vm = PaginatedAllTransfersViewModel.build(
        items      = items,
        page       = 1,
        pageSize   = pageSize,
        urlForPage = urlFor
      )

      vm.pagination mustBe None
      vm.table.rows.size mustBe items.size
      vm.lockWarning mustBe None
    }

    "must return pagination with correct items, current page marking, and prev/next links" in {
      val items    = (1 to 23).map(i => mkItem(i, utc(2025, 9, 24, 10, 15)))
      val pageSize = 10
      val page     = 2

      val vm = PaginatedAllTransfersViewModel.build(
        items      = items,
        page       = page,
        pageSize   = pageSize,
        urlForPage = urlFor
      )

      vm.pagination.isDefined mustBe true
      val p = vm.pagination.get

      p.items.isDefined mustBe true
      val pagers = p.items.get
      pagers.map(_.number.get) mustBe Seq("1", "2", "3")
      pagers.map(_.href) mustBe Seq("/dash?page=1", "/dash?page=2", "/dash?page=3")
      pagers.map(_.current) mustBe Seq(Some(false), Some(true), Some(false))

      p.previous.isDefined mustBe true
      p.previous.get.href mustBe "/dash?page=1"
      p.previous.get.labelText mustBe Some("site.previous")

      p.next.isDefined mustBe true
      p.next.get.href mustBe "/dash?page=3"
      p.next.get.labelText mustBe Some("site.next")
    }

    "must sort items by latest before paginating (newest first on page 1)" in {
      val instants = Seq(
        utc(2025, 3, 5, 12, 0),
        utc(2025, 1, 1, 0, 0),
        utc(2025, 2, 10, 8, 30),
        utc(2025, 3, 1, 23, 45),
        utc(2025, 2, 28, 19, 10),
        utc(2025, 1, 31, 6, 15)
      )
      val items    = instants.zipWithIndex.map { case (ins, i) => mkItem(i + 1, ins) }

      val vm = PaginatedAllTransfersViewModel.build(
        items      = items,
        page       = 1,
        pageSize   = 3,
        urlForPage = urlFor
      )

      val expectedTop3: Seq[(String, String)] =
        instants.sorted(Ordering[Instant]).reverse.take(3).map(fmt)

      val actualTop3: Seq[(String, String)] = getLastUpdatedCol(vm)

      actualTop3 mustBe expectedTop3
      vm.table.rows.size mustBe 3
    }

    "must maintain strict descending order across pages (date & time)" in {
      val items    = (1 to 23).map { d =>
        mkItem(d, utc(2025, 9, d, 10, 15))
      }
      val pageSize = 10

      val vmPage1 = PaginatedAllTransfersViewModel.build(
        items      = items,
        page       = 1,
        pageSize   = pageSize,
        urlForPage = urlFor
      )
      val vmPage2 = PaginatedAllTransfersViewModel.build(
        items      = items,
        page       = 2,
        pageSize   = pageSize,
        urlForPage = urlFor
      )

      val expectedPage1 = (23 to 14 by -1).map(d => fmt(utc(2025, 9, d, 10, 15)))
      val expectedPage2 = (13 to 4 by -1).map(d => fmt(utc(2025, 9, d, 10, 15)))

      val actualPage1 = getLastUpdatedCol(vmPage1)
      val actualPage2 = getLastUpdatedCol(vmPage2)

      actualPage1.size mustBe 10
      actualPage2.size mustBe 10

      actualPage1 mustBe expectedPage1
      actualPage2 mustBe expectedPage2

      actualPage1.last._1 mustBe "14 September 2025"
      actualPage2.head._1 mustBe "13 September 2025"
      actualPage1.last._2 mustBe "10:15am"
      actualPage2.head._2 mustBe "10:15am"
    }

    "must include lockWarning in the view model when provided" in {
      val items = (1 to 3).map(i => mkItem(i, utc(2025, 10, i, 9)))
      val lock  = Some("Record is locked by another user")

      val vm = PaginatedAllTransfersViewModel.build(
        items       = items,
        page        = 1,
        pageSize    = 10,
        urlForPage  = urlFor,
        lockWarning = lock
      )

      vm.lockWarning mustBe lock
    }
  }
}

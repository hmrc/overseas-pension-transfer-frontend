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

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PaginatedAllTransfersViewModelSpec extends AnyFreeSpec with SpecBase with Matchers {

  implicit val messages: Messages = stubMessagesApi().preferred(Seq.empty)

  private def mkItem(idx: Int, date: LocalDate): AllTransfersItem =
    AllTransfersItem(
      transferReference = Some(s"TR-$idx"),
      qtReference       = None,
      qtVersion         = None,
      nino              = None,
      memberFirstName   = Some(s"Name$idx"),
      memberSurname     = Some("McUser"),
      submissionDate    = None,
      lastUpdated       = Some(date),
      qtStatus          = None,
      pstrNumber        = None
    )

  private def urlFor(n: Int): String = s"/dash?page=$n"

  private val fmt: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM uuuu")

  private def lastUpdatedText(row: uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow): String =
    row.content.asHtml.toString

  private def dateColOf(vm: PaginatedAllTransfersViewModel): Seq[String] =
    vm.table.rows.map(_.last).map(lastUpdatedText)

  "PaginatedAllTransfersViewModel.build" - {

    "must return no pagination when there is only a single page" in {
      val items    = (1 to 5).map(i => mkItem(i, LocalDate.of(2025, 1, i)))
      val pageSize = 10

      val vm = PaginatedAllTransfersViewModel.build(
        items      = items,
        page       = 1,
        pageSize   = pageSize,
        urlForPage = urlFor
      )

      vm.pagination mustBe None
      vm.table.rows.size mustBe items.size
    }

    "must return pagination with correct items, current page marking, and prev/next links" in {
      val items    = (1 to 23).map(i => mkItem(i, LocalDate.of(2025, 1, i)))
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

    "must sort items by latest date before paginating (newest first on page 1)" in {
      val dates = Seq(
        LocalDate.of(2025, 3, 5),
        LocalDate.of(2025, 1, 1),
        LocalDate.of(2025, 2, 10),
        LocalDate.of(2025, 3, 1),
        LocalDate.of(2025, 2, 28),
        LocalDate.of(2025, 1, 31)
      )
      val items = dates.zipWithIndex.map { case (d, i) => mkItem(i + 1, d) }

      val vm           = PaginatedAllTransfersViewModel.build(
        items      = items,
        page       = 1,
        pageSize   = 3,
        urlForPage = urlFor
      )
      val expectedTop3 = dates.sortBy(identity).reverse.take(3)

      val top3SortedItems = items.sorted.take(3)
      val renderedTop3    = top3SortedItems.map(_.lastUpdatedDate.get)

      renderedTop3 mustBe expectedTop3
      vm.table.rows.size mustBe 3
    }

    "must maintain strict descending order across pages" in {
      val items    = (1 to 23).map(i => mkItem(i, LocalDate.of(2025, 1, i)))
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

      val expectedPage1 = (23 to 14 by -1).map(d => fmt.format(LocalDate.of(2025, 1, d)))
      val expectedPage2 = (13 to 4 by -1).map(d => fmt.format(LocalDate.of(2025, 1, d)))

      val page1Dates = dateColOf(vmPage1)
      val page2Dates = dateColOf(vmPage2)

      page1Dates.size mustBe 10
      page2Dates.size mustBe 10

      page1Dates mustBe expectedPage1
      page2Dates mustBe expectedPage2

      page1Dates.last mustBe "14 January 2025"
      page2Dates.head mustBe "13 January 2025"
    }
  }
}

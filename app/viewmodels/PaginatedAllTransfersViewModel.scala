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
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination._
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.Table
import utils.{Paging, PagingRequest}

final case class PaginatedAllTransfersViewModel(
    table: Table,
    pagination: Option[Pagination], // None when single page
    lockWarning: Option[String] = None
  )

object PaginatedAllTransfersViewModel {

  def build(
      items: Seq[AllTransfersItem],
      page: Int,
      pageSize: Int,
      urlForPage: Int => String,
      lockWarning: Option[String] = None
    )(implicit messages: Messages
    ): PaginatedAllTransfersViewModel = {

    val sorted = items.sorted
    val paging = Paging.fromSeq(sorted, PagingRequest(page, pageSize))
    val table  = AllTransfersTableViewModel.from(paging.items, page)
    val pager  = paginationFrom(paging, urlForPage)
    PaginatedAllTransfersViewModel(table, pager, lockWarning)
  }

  private def paginationFrom[A](p: Paging[A], urlForPage: Int => String)(implicit m: Messages): Option[Pagination] = {
    if (p.totalPages <= 1) {
      None
    } else {
      val items: Seq[PaginationItem] =
        (1 to p.totalPages).map { n =>
          PaginationItem(
            number  = Some(n.toString),
            href    = urlForPage(n),
            current = Some(n == p.page)
          )
        }

      val prev = if (p.hasPrev) {
        Some(PaginationLink(href = urlForPage(p.page - 1), labelText = Some(m("site.previous"))))
      } else {
        None
      }

      val next = if (p.hasNext) {
        Some(PaginationLink(href = urlForPage(p.page + 1), labelText = Some(m("site.next"))))
      } else {
        None
      }

      Some(
        Pagination(
          items         = Some(items),
          previous      = prev,
          next          = next,
          landmarkLabel = Some(m("pagination.landmark"))
        )
      )
    }
  }
}

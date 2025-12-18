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
import models.AllTransfersItem
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination._
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.Table
import utils.{Paging, PagingRequest}

final case class PaginatedAllTransfersViewModel(
    table: Table,
    pagination: Option[Pagination],
    lockWarning: Option[String] = None,
    currentPage: Int            = 1,
    totalPages: Int             = 1
  )

object PaginatedAllTransfersViewModel {

  def build(
      items: Seq[AllTransfersItem],
      page: Int,
      pageSize: Int,
      urlForPage: Int => String,
      lockWarning: Option[String] = None
    )(implicit messages: Messages,
      appConfig: FrontendAppConfig
    ): PaginatedAllTransfersViewModel = {

    val sorted = items.sorted
    val paging = Paging.fromSeq(sorted, PagingRequest(page, pageSize))
    val table  = AllTransfersTableViewModel.from(paging.items, page)
    val pager  = paginationFrom(paging, urlForPage)
    PaginatedAllTransfersViewModel(table, pager, lockWarning, paging.page, paging.totalPages)
  }

  private def paginationFrom[A](p: Paging[A], urlForPage: Int => String)(implicit m: Messages): Option[Pagination] = {
    if (p.totalPages <= 1) {
      None
    } else {
      val items: Seq[PaginationItem] = createSmartPaginationItems(p.page, p.totalPages, urlForPage)

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

  private def createSmartPaginationItems(
      currentPage: Int,
      totalPages: Int,
      urlForPage: Int => String
    ): Seq[PaginationItem] = {
    val mustShow = Set(
      1,
      currentPage,
      totalPages,
      currentPage - 1,
      currentPage + 1
    ).filter(p => p >= 1 && p <= totalPages)
      .toSeq
      .sorted

    val itemsWithEllipsis = mustShow.foldLeft(Seq.empty[Either[Unit, Int]]) { (acc, page) =>
      acc.lastOption match {
        case Some(Right(lastPage)) if page - lastPage > 1 =>
          acc :+ Left(()) :+ Right(page)
        case _                                            =>
          acc :+ Right(page)
      }
    }

    itemsWithEllipsis.map {
      case Right(pageNum) =>
        PaginationItem(
          href     = urlForPage(pageNum),
          number   = Some(pageNum.toString),
          current  = Some(pageNum == currentPage),
          ellipsis = None
        )
      case Left(_)        =>
        PaginationItem(
          href     = "",
          number   = None,
          current  = None,
          ellipsis = Some(true)
        )
    }
  }
}

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

package utils

final case class PagingRequest(page: Int, pageSize: Int) {
  require(page >= 1, "page is 1-based and must be >= 1")
  require(pageSize >= 1, "pageSize must be >= 1")
}

final case class Paging[A](items: Seq[A], total: Int, page: Int, pageSize: Int) {
  val totalPages: Int  = math.max(1, (total + pageSize - 1) / pageSize)
  val hasPrev: Boolean = page > 1
  val hasNext: Boolean = page < totalPages
}

object Paging {

  def fromSeq[A](all: Seq[A], req: PagingRequest): Paging[A] = {
    val start        = (req.page - 1) * req.pageSize
    val clampedStart = start.max(0).min(all.size)
    val slice        = all.slice(clampedStart, clampedStart + req.pageSize)
    Paging(slice, total = all.size, page = req.page, pageSize = req.pageSize)
  }
}

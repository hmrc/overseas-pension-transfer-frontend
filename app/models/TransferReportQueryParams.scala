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

package models

import play.api.mvc.{Call, Request}

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

case class TransferReportQueryParams(
    transferId: Option[TransferId],
    qtStatus: Option[QtStatus],
    pstr: Option[PstrNumber],
    versionNumber: Option[String],
    memberName: String,
    currentPage: Int
  )

object TransferReportQueryParams {

  def fromRequest(request: Request[_]): TransferReportQueryParams = {
    TransferReportQueryParams(
      transferId    = request.getQueryString("transferId").map(TransferId(_)),
      qtStatus      = request.getQueryString("qtStatus").flatMap(s => QtStatus.values.find(_.toString == s)),
      pstr          = request.getQueryString("pstr").map(PstrNumber(_)),
      versionNumber = request.getQueryString("versionNumber"),
      memberName    = request.getQueryString("memberName").getOrElse("-"),
      currentPage   = request.getQueryString("currentPage").flatMap(_.toIntOption).getOrElse(1)
    )
  }

  /** Helper to URL-encode */
  private def enc(v: String): String =
    URLEncoder.encode(v, StandardCharsets.UTF_8.toString)

  def toQueryString(p: TransferReportQueryParams): String = {
    val params = Seq(
      p.transferId.map(tr => s"transferId=${enc(tr.value)}"),
      p.qtStatus.map(qs => s"qtStatus=${enc(qs.toString)}"),
      p.pstr.map(ps => s"pstr=${enc(ps.value)}"),
      p.versionNumber.map(vn => s"versionNumber=${enc(vn)}"),
      Some(s"memberName=${enc(p.memberName)}"),
      Some(s"currentPage=${p.currentPage}")
    ).flatten.mkString("&")

    s"?$params"
  }

  def toUrl(p: TransferReportQueryParams): String =
    controllers.routes.DashboardController.onTransferClick().url + toQueryString(p)
}

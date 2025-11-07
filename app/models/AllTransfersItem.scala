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

import play.api.libs.json.{Json, OFormat}

import java.time.{Instant, LocalDate}

/** Exactly one of `submissionDate` or `lastUpdated` should be defined. Submitted/Compiled => submissionDate InProgress => lastUpdated
  */
case class AllTransfersItem(
    transferId: TransferId,
    qtVersion: Option[String],
    qtStatus: Option[QtStatus],
    nino: Option[String],
    memberFirstName: Option[String],
    memberSurname: Option[String],
    qtDate: Option[LocalDate],
    lastUpdated: Option[Instant],
    pstrNumber: Option[PstrNumber],
    submissionDate: Option[Instant]
  ) {

  def isValid: Boolean =
    submissionDate.isDefined ^ lastUpdated.isDefined

  def lastUpdatedDate: Option[Instant] = lastUpdated.orElse(submissionDate)

  def viewExpiringTransferUrl: String = {
    val baseUrl = "/report-transfer-qualified-recognised-overseas-pension-scheme/dashboard/transfer-report"
    val params  = TransferReportQueryParams(
      transferId    = Some(transferId),
      qtStatus      = qtStatus,
      pstr          = pstrNumber,
      versionNumber = qtVersion,
      memberName    = s"${memberFirstName.getOrElse("")} ${memberSurname.getOrElse("")}".trim,
      currentPage   = 1
    )
    s"$baseUrl?${TransferReportQueryParams.toQueryString(params).drop(1)}"
  }
}

object AllTransfersItem {
  implicit val format: OFormat[AllTransfersItem] = Json.format[AllTransfersItem]

  implicit val ordering: Ordering[AllTransfersItem] =
    Ordering.by[AllTransfersItem, Option[Instant]](t => t.lastUpdatedDate)(
      Ordering.Option(Ordering[Instant]).reverse
    )
}

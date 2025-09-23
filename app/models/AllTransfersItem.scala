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

import java.time.LocalDate

case class AllTransfersItem(
    transferReference: Option[String],
    qtReference: Option[QtNumber],
    qtVersion: Option[String],
    nino: Option[String],
    memberFirstName: Option[String],
    memberSurname: Option[String],
    submissionDate: Option[LocalDate],
    lastUpdated: Option[LocalDate],
    qtStatus: Option[QtStatus],
    pstrNumber: Option[PstrNumber]
  ) {
  def displayLastUpdatedDate: Option[LocalDate] = lastUpdated.orElse(submissionDate)
}

object AllTransfersItem {
  implicit val format: OFormat[AllTransfersItem] = Json.format
}

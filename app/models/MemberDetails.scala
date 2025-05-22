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

import models.address.Address

import java.time.LocalDate
import play.api.libs.json.{Json, OFormat}

case class MemberDetails(
    memberName: Option[PersonName]               = None, // PersonName.empty,
    memberDateOfBirth: Option[LocalDate]         = None, // LocalDate.MIN,
    memberNino: Option[String]                   = None,
    memberDoesNotHaveNino: Option[String]        = None,
    membersCurrentAddress: Option[Address]       = None, // Address.empty,
    memberIsResidentUK: Option[Boolean]          = None, // true,
    memberHasEverBeenResidentUK: Option[Boolean] = None,
    membersLastUKAddress: Option[Address]        = None,
    memberDateOfLeavingUK: Option[LocalDate]     = None
  )

object MemberDetails {
  implicit val format: OFormat[MemberDetails] = Json.format[MemberDetails]
  val empty: MemberDetails                    = MemberDetails()
}

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

package models.audit

import models.{Enumerable, WithName}
import play.api.libs.json._

sealed trait JourneyStartedType

object JourneyStartedType {

  case object StartNewTransfer            extends WithName("startNewTransferReport") with JourneyStartedType
  case object ContinueTransfer            extends WithName("continueTransferReportInProgress") with JourneyStartedType
  case object StartAmendmentOfTransfer    extends WithName("amendSubmittedTransferReport") with JourneyStartedType
  case object ContinueAmendmentOfTransfer extends WithName("progressAmendedSubmission") with JourneyStartedType
  case object StartJourneyFailed          extends WithName("startJourneyFailed") with JourneyStartedType

  val values: Seq[JourneyStartedType] =
    Seq(StartNewTransfer, ContinueTransfer, StartAmendmentOfTransfer, ContinueAmendmentOfTransfer, StartJourneyFailed)

  val enumerable: Enumerable[JourneyStartedType] =
    Enumerable(values.map(v => v.toString -> v): _*)

  implicit def reads: Reads[JourneyStartedType] = Reads[JourneyStartedType] {
    case JsString(StartNewTransfer.toString)            => JsSuccess(StartNewTransfer)
    case JsString(ContinueTransfer.toString)            => JsSuccess(ContinueTransfer)
    case JsString(StartAmendmentOfTransfer.toString)    => JsSuccess(StartAmendmentOfTransfer)
    case JsString(ContinueAmendmentOfTransfer.toString) => JsSuccess(ContinueAmendmentOfTransfer)
    case JsString(StartJourneyFailed.toString)          => JsSuccess(StartJourneyFailed)
    case _                                              => JsError("error.invalid")
  }

  implicit def writes: Writes[JourneyStartedType] = {
    Writes(value => JsString(value.toString))
  }
}

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

package models.email

import play.api.libs.json.{JsResult, JsValue, Json, Reads, Writes}

sealed trait EmailParameters

object EmailParameters {

  implicit val reads: Reads[EmailParameters] = new Reads[EmailParameters] {

    override def reads(json: JsValue): JsResult[EmailParameters] = {
      json.validate[SubmissionConfirmation].orElse(
        json.validate[SubmissionConfirmation]
      )
    }
  }

  implicit val writes: Writes[EmailParameters] = Writes[EmailParameters] {
    case submissionConfirmation: SubmissionConfirmation =>
      Json.toJson(submissionConfirmation)(SubmissionConfirmation.writes)
  }
}

case class SubmissionConfirmation(
    qtReference: String,
    memberName: String,
    submitter: String,
    submissionDate: String,
    pensionSchemeName: String
  ) extends EmailParameters

object SubmissionConfirmation {

  implicit val reads: Reads[SubmissionConfirmation]   = Json.reads[SubmissionConfirmation]
  implicit val writes: Writes[SubmissionConfirmation] = Json.writes[SubmissionConfirmation]
}

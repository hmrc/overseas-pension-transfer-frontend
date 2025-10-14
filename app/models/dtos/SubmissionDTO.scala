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

package models.dtos

import models.{SessionData, UserAnswers}
import models.authentication._
import play.api.libs.json._

import java.time.Instant

sealed trait SubmissionDTO {
  def referenceId: String
  def userType: UserType
  def lastUpdated: Instant
}

object SubmissionDTO {

  def fromRequest(authenticatedUser: AuthenticatedUser, userAnswers: UserAnswers, maybePsaId: Option[PsaId], sessionData: SessionData): SubmissionDTO =
    authenticatedUser match {
      case PsaUser(psaId, _, _, _) =>
        PsaSubmissionDTO(
          referenceId = sessionData.transferId,
          userId      = psaId,
          lastUpdated = userAnswers.lastUpdated
        )

      case PspUser(pspId, _, _s, _) =>
        PspSubmissionDTO(
          referenceId = sessionData.transferId,
          userId      = pspId,
          psaId       = maybePsaId.get,
          lastUpdated = userAnswers.lastUpdated
        )
    }

  implicit val format: OFormat[SubmissionDTO] = {
    val psaReads = Json.reads[PsaSubmissionDTO].map(identity: PsaSubmissionDTO => SubmissionDTO)
    val pspReads = Json.reads[PspSubmissionDTO].map(identity: PspSubmissionDTO => SubmissionDTO)

    val reads: Reads[SubmissionDTO] = (__ \ "userType").read[UserType].flatMap {
      case Psa => psaReads
      case Psp => pspReads
      case _   => Reads(_ => JsError("Invalid userType"))
    }

    val writes: OWrites[SubmissionDTO] = {
      case psa: PsaSubmissionDTO => Json.writes[PsaSubmissionDTO].writes(psa)
      case psp: PspSubmissionDTO => Json.writes[PspSubmissionDTO].writes(psp)
    }

    OFormat(reads, writes)
  }

}

case class PsaSubmissionDTO(
    referenceId: String,
    userType: UserType = Psa,
    userId: PsaId,
    lastUpdated: Instant
  ) extends SubmissionDTO

case class PspSubmissionDTO(
    referenceId: String,
    userType: UserType = Psp,
    userId: PspId,
    psaId: PsaId,
    lastUpdated: Instant
  ) extends SubmissionDTO

object PsaSubmissionDTO {
  implicit val format: OFormat[PsaSubmissionDTO] = Json.format
}

object PspSubmissionDTO {
  implicit val format: OFormat[PspSubmissionDTO] = Json.format
}

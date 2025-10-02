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

import models.audit.JourneyStartedType.StartNewTransfer
import models.authentication.{AuthenticatedUser, PsaUser, PspUser}
import play.api.libs.json.{JsValue, Json}

case class TransferStartedAuditModel(authenticatedUser: AuthenticatedUser) extends JsonAuditModel {

  override val auditType: String = "OverseasPensionTransferReportStarted"

  private val pensionSchemeName         = authenticatedUser.pensionSchemeDetails.map(_.schemeName).getOrElse("")
  private val pstr                      = authenticatedUser.pensionSchemeDetails.map(_.pstrNumber.value).getOrElse("")
  private val userRole                  = authenticatedUser.userType
  // TODO UPDATE ID AFTER NICKS TICKET
  private val internalReportReferenceId = "testID"

  private val (userId, affinityGroup) = authenticatedUser match {
    case PsaUser(psaId, _, _, affinityGroup) => (psaId.value, affinityGroup)
    case PspUser(pspId, _, _, affinityGroup) => (pspId.value, affinityGroup)
  }

  override val detail: JsValue = Json.obj(
    "journey"                   -> StartNewTransfer.toString,
    "internalReportReferenceId" -> internalReportReferenceId,
    "pensionSchemeName"         -> pensionSchemeName,
    "pensionSchemeTaxReference" -> pstr,
    "roleLoggedInAs"            -> userRole,
    "affinityGroup"             -> affinityGroup,
    "requesterIdentifier"       -> userId
  )
}

object TransferStartedAuditModel {

  def build(authenticatedUser: AuthenticatedUser): TransferStartedAuditModel =
    TransferStartedAuditModel(authenticatedUser)
}

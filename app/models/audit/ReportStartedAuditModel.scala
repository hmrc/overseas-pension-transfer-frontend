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

import models.AllTransfersItem
import models.authentication.{AuthenticatedUser, PsaUser, PspUser}
import play.api.libs.json.{JsValue, Json}

case class ReportStartedAuditModel(
    authenticatedUser: AuthenticatedUser,
    journey: JourneyStartedType,
    allTransfersItem: Option[AllTransfersItem],
    failure: Option[String]
  ) extends JsonAuditModel {

  override val auditType: String        = "OverseasPensionTransferReportStarted"
  // TODO UPDATE ID AFTER NICKS TICKET - remember its optional, wont exist for submitted reports.
  private val internalReportReferenceId = "testID"
  private val userRole                  = authenticatedUser.userType

  private val pensionSchemeName =
    authenticatedUser.pensionSchemeDetails
      .map(details => Json.obj("pensionSchemeName" -> details.schemeName))
      .getOrElse(Json.obj())

  private val pstr =
    authenticatedUser.pensionSchemeDetails
      .map(details => Json.obj("pensionSchemeTaxReference" -> details.pstrNumber.value))
      .getOrElse(Json.obj())

  private val (userId, affinityGroup) = authenticatedUser match {
    case PsaUser(psaId, _, _, affinityGroup) => (psaId.value, affinityGroup)
    case PspUser(pspId, _, _, affinityGroup) => (pspId.value, affinityGroup)
  }

  private val memberFirstName =
    allTransfersItem
      .map(item => {
        val name: String = item.memberFirstName.getOrElse("")
        Json.obj("memberFirstName" -> name)
      }).getOrElse(Json.obj())

  private val memberSurname =
    allTransfersItem
      .map(item => {
        val surname: String = item.memberSurname.getOrElse("")
        Json.obj("memberSurname" -> surname)
      }).getOrElse(Json.obj())

  private val memberNino =
    allTransfersItem
      .map(item => {
        val nino: String = item.nino.getOrElse("")
        Json.obj("memberNino" -> nino)
      }).getOrElse(Json.obj())

  private val qtNumber =
    allTransfersItem.flatMap(_.qtReference)
      .map(qt => Json.obj("overseasPensionTransferReportReference" -> qt.value))
      .getOrElse(Json.obj())

  private val failureReason =
    failure.map(reason => { Json.obj("reasonForFailure" -> reason) }).getOrElse(Json.obj())

  override val detail: JsValue = Json.obj(
    "journey"                   -> journey.toString,
    "internalReportReferenceId" -> internalReportReferenceId,
    "roleLoggedInAs"            -> userRole,
    "affinityGroup"             -> affinityGroup,
    "requesterIdentifier"       -> userId
  ) ++ pensionSchemeName ++ pstr ++ memberFirstName ++ memberSurname ++ memberNino ++ qtNumber ++ failureReason
}

object ReportStartedAuditModel {

  def build(
      authenticatedUser: AuthenticatedUser,
      journey: JourneyStartedType,
      allTransfersItem: Option[AllTransfersItem],
      failure: Option[String] = None
    ): ReportStartedAuditModel =
    ReportStartedAuditModel(authenticatedUser, journey, allTransfersItem, failure)
}

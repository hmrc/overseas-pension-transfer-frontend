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
import models.{AllTransfersItem, PensionSchemeDetails, PstrNumber, QtNumber, SrnNumber}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import models.authentication.{PsaId, PsaUser, PspId, PspUser}
import play.api.libs.json.Json
import uk.gov.hmrc.auth.core.AffinityGroup.{Individual, Organisation}

class ReportStartedAuditModelSpec extends AnyFreeSpec with Matchers {

  private val pensionSchemeDetails = PensionSchemeDetails(
    SrnNumber("SRN123"),
    PstrNumber("PSTR123"),
    "Pension Scheme A"
  )
  private val authenticatedPsa     = PsaUser(PsaId("21000005"), "internalId", None, Individual)
  private val authenticatedPsp     = PspUser(PspId("21000005"), "internalId", None, Individual)

  private val allTransfersItem = AllTransfersItem(
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None
  )

  "must create correct minimal json for different journey types" in {
    JourneyStartedType.values.foreach {
      journey =>
        val expectedJson = Json.obj(
          "journey"                   -> journey.toString,
          "internalReportReferenceId" -> "testID",
          "roleLoggedInAs"            -> "Psa",
          "affinityGroup"             -> "Individual",
          "requesterIdentifier"       -> "21000005"
        )

        val result = ReportStartedAuditModel.build(authenticatedPsa, journey, None)
        result.auditType mustBe "OverseasPensionTransferReportStarted"
        result.detail mustBe expectedJson
    }
  }

  "must create correct minimal json for Individual and PSA" in {
    val expectedJson = Json.obj(
      "journey"                   -> "startNewTransferReport",
      "internalReportReferenceId" -> "testID",
      "roleLoggedInAs"            -> "Psa",
      "affinityGroup"             -> "Individual",
      "requesterIdentifier"       -> "21000005"
    )

    val result = ReportStartedAuditModel.build(authenticatedPsa, StartNewTransfer, None)
    result.auditType mustBe "OverseasPensionTransferReportStarted"
    result.detail mustBe expectedJson
  }

  "must create correct minimal json for Organisation and PSP" in {
    val expectedJson = Json.obj(
      "journey"                   -> "startNewTransferReport",
      "internalReportReferenceId" -> "testID",
      "roleLoggedInAs"            -> "Psp",
      "affinityGroup"             -> "Organisation",
      "requesterIdentifier"       -> "21000005"
    )

    val result = ReportStartedAuditModel.build(
      authenticatedPsp.copy(affinityGroup = Organisation),
      StartNewTransfer,
      None
    )
    result.auditType mustBe "OverseasPensionTransferReportStarted"
    result.detail mustBe expectedJson
  }

  "must create correct json with pension scheme details" in {
    val user         = authenticatedPsp.copy(affinityGroup = Organisation, pensionSchemeDetails = Some(pensionSchemeDetails))
    val expectedJson = Json.obj(
      "journey"                   -> "startNewTransferReport",
      "internalReportReferenceId" -> "testID",
      "pensionSchemeName"         -> "Pension Scheme A",
      "pensionSchemeTaxReference" -> "PSTR123",
      "roleLoggedInAs"            -> "Psp",
      "affinityGroup"             -> "Organisation",
      "requesterIdentifier"       -> "21000005"
    )

    val result = ReportStartedAuditModel.build(user, StartNewTransfer, None)

    result.auditType mustBe "OverseasPensionTransferReportStarted"
    result.detail mustBe expectedJson
  }

  "must create full correct json with member details and QTNumber" in {
    val user         = authenticatedPsp.copy(affinityGroup = Organisation, pensionSchemeDetails = Some(pensionSchemeDetails))
    val item         = allTransfersItem.copy(
      nino            = Some("testNino"),
      memberFirstName = Some("Rob"),
      memberSurname   = Some("Darby"),
      qtReference     = Some(QtNumber("QT123456"))
    )
    val expectedJson = Json.obj(
      "journey"                                -> "startNewTransferReport",
      "internalReportReferenceId"              -> "testID",
      "pensionSchemeName"                      -> "Pension Scheme A",
      "pensionSchemeTaxReference"              -> "PSTR123",
      "roleLoggedInAs"                         -> "Psp",
      "affinityGroup"                          -> "Organisation",
      "requesterIdentifier"                    -> "21000005",
      "memberFirstName"                        -> "Rob",
      "memberSurname"                          -> "Darby",
      "memberNino"                             -> "testNino",
      "overseasPensionTransferReportReference" -> "QT123456"
    )

    val result = ReportStartedAuditModel.build(user, StartNewTransfer, Some(item))

    result.auditType mustBe "OverseasPensionTransferReportStarted"
    result.detail mustBe expectedJson
  }

  "must create full correct json with member details without QTNumber" in {
    val user         = authenticatedPsp.copy(affinityGroup = Organisation, pensionSchemeDetails = Some(pensionSchemeDetails))
    val item         = allTransfersItem.copy(
      nino            = Some("testNino"),
      memberFirstName = Some("Rob"),
      memberSurname   = Some("Darby")
    )
    val expectedJson = Json.obj(
      "journey"                   -> "startNewTransferReport",
      "internalReportReferenceId" -> "testID",
      "pensionSchemeName"         -> "Pension Scheme A",
      "pensionSchemeTaxReference" -> "PSTR123",
      "roleLoggedInAs"            -> "Psp",
      "affinityGroup"             -> "Organisation",
      "requesterIdentifier"       -> "21000005",
      "memberFirstName"           -> "Rob",
      "memberSurname"             -> "Darby",
      "memberNino"                -> "testNino"
    )

    val result = ReportStartedAuditModel.build(user, StartNewTransfer, Some(item))

    result.auditType mustBe "OverseasPensionTransferReportStarted"
    result.detail mustBe expectedJson
  }

  "must create correct json with failure reason for startJourneyFailed" in {
    val user         = authenticatedPsp.copy(affinityGroup = Organisation, pensionSchemeDetails = Some(pensionSchemeDetails))
    val expectedJson = Json.obj(
      "journey"                   -> "startNewTransferReport",
      "internalReportReferenceId" -> "testID",
      "reasonForFailure"          -> "503: Forbidden request",
      "pensionSchemeName"         -> "Pension Scheme A",
      "pensionSchemeTaxReference" -> "PSTR123",
      "roleLoggedInAs"            -> "Psp",
      "affinityGroup"             -> "Organisation",
      "requesterIdentifier"       -> "21000005"
    )
    val failure      = Some("503: Forbidden request")
    val result       = ReportStartedAuditModel.build(user, StartNewTransfer, None, failure)

    result.auditType mustBe "OverseasPensionTransferReportStarted"
    result.detail mustBe expectedJson
  }
}

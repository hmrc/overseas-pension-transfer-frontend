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

import base.SpecBase
import models.audit.JourneyStartedType.StartNewTransfer
import models.{AllTransfersItem, PstrNumber, QtNumber}
import play.api.libs.json.Json
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation

class ReportStartedAuditModelSpec extends SpecBase {
  private val allTransfersItem = AllTransfersItem(
    testTransferNumber,
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

  "must create correct minimal json for different journey types" in
    JourneyStartedType.values.foreach { journey =>
      val expectedJson = Json.obj(
        "journeyType"               -> journey.toString,
        "internalReportReferenceId" -> testTransferNumber.value,
        "roleLoggedInAs"            -> "Psa",
        "affinityGroup"             -> "Individual",
        "requesterIdentifier"       -> psaId.value,
        "pensionSchemeName"         -> schemeName,
        "pensionSchemeTaxReference" -> "12345678AB"
      )

      val result =
        ReportStartedAuditModel.build(testTransferNumber, psaUser, schemeDetails, journey, None)
      result.auditType mustBe "OverseasPensionTransferReportStarted"
      result.detail mustBe expectedJson
    }

  "must create correct minimal json for Individual and PSA" in {
    val expectedJson = Json.obj(
      "journeyType"               -> "startNewTransferReport",
      "internalReportReferenceId" -> testTransferNumber.value,
      "roleLoggedInAs"            -> "Psa",
      "affinityGroup"             -> "Individual",
      "requesterIdentifier"       -> psaId.value,
      "pensionSchemeName"         -> schemeName,
      "pensionSchemeTaxReference" -> "12345678AB"
    )

    val result =
      ReportStartedAuditModel.build(testTransferNumber, psaUser, schemeDetails, StartNewTransfer, None)
    result.auditType mustBe "OverseasPensionTransferReportStarted"
    result.detail mustBe expectedJson
  }

  "must create correct minimal json for Organisation and PSP" in {
    val expectedJson = Json.obj(
      "journeyType"               -> "startNewTransferReport",
      "internalReportReferenceId" -> testTransferNumber.value,
      "roleLoggedInAs"            -> "Psp",
      "affinityGroup"             -> "Organisation",
      "requesterIdentifier"       -> pspId.value,
      "pensionSchemeName"         -> schemeName,
      "pensionSchemeTaxReference" -> "12345678AB"
    )

    val result = ReportStartedAuditModel.build(
      testTransferNumber,
      pspUser.copy(affinityGroup = Organisation),
      schemeDetails,
      StartNewTransfer,
      None
    )
    result.auditType mustBe "OverseasPensionTransferReportStarted"
    result.detail mustBe expectedJson
  }

  "must create full correct json with member details and QTNumber" in {
    val user         = pspUser.copy(affinityGroup = Organisation)
    val item         = allTransfersItem.copy(
      nino = Some("testNino"),
      memberFirstName = Some("Rob"),
      memberSurname = Some("Darby"),
      transferId = QtNumber("QT123456")
    )
    val expectedJson = Json.obj(
      "journeyType"               -> "startNewTransferReport",
      "internalReportReferenceId" -> "QT123456",
      "roleLoggedInAs"            -> "Psp",
      "affinityGroup"             -> "Organisation",
      "requesterIdentifier"       -> pspId.value,
      "pensionSchemeName"         -> schemeName,
      "pensionSchemeTaxReference" -> "12345678AB",
      "memberFirstName"           -> "Rob",
      "memberSurname"             -> "Darby",
      "memberNino"                -> "testNino"
    )

    val result = ReportStartedAuditModel.build(QtNumber("QT123456"), user, schemeDetails, StartNewTransfer, Some(item))

    result.auditType mustBe "OverseasPensionTransferReportStarted"
    result.detail mustBe expectedJson
  }

  "must create full correct json with member details without QTNumber" in {
    val user         = pspUser.copy(affinityGroup = Organisation)
    val item         = allTransfersItem.copy(
      nino = Some("testNino"),
      memberFirstName = Some("Rob"),
      memberSurname = Some("Darby")
    )
    val expectedJson = Json.obj(
      "journeyType"               -> "startNewTransferReport",
      "internalReportReferenceId" -> testTransferNumber.value,
      "roleLoggedInAs"            -> "Psp",
      "affinityGroup"             -> "Organisation",
      "requesterIdentifier"       -> pspId.value,
      "pensionSchemeName"         -> schemeName,
      "pensionSchemeTaxReference" -> "12345678AB",
      "memberFirstName"           -> "Rob",
      "memberSurname"             -> "Darby",
      "memberNino"                -> "testNino"
    )

    val result =
      ReportStartedAuditModel.build(testTransferNumber, user, schemeDetails, StartNewTransfer, Some(item))

    result.auditType mustBe "OverseasPensionTransferReportStarted"
    result.detail mustBe expectedJson
  }

  "must create correct json with failure reason for startJourneyFailed" in {
    val user         = pspUser.copy(affinityGroup = Organisation)
    val expectedJson = Json.obj(
      "journeyType"               -> "startNewTransferReport",
      "internalReportReferenceId" -> testTransferNumber.value,
      "reasonForFailure"          -> "503: Forbidden request",
      "roleLoggedInAs"            -> "Psp",
      "affinityGroup"             -> "Organisation",
      "requesterIdentifier"       -> pspId.value,
      "pensionSchemeName"         -> "Pension Scheme A",
      "pensionSchemeTaxReference" -> "PSTR123"
    )
    val failure      = Some("503: Forbidden request")
    val result       =
      ReportStartedAuditModel.build(
        testTransferNumber,
        user,
        schemeDetails.copy(pstrNumber = PstrNumber("PSTR123"), schemeName = "Pension Scheme A"),
        StartNewTransfer,
        None,
        failure
      )

    result.auditType mustBe "OverseasPensionTransferReportStarted"
    result.detail mustBe expectedJson
  }
}

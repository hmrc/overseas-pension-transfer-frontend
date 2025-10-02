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

import models.{PensionSchemeDetails, PstrNumber, SrnNumber}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import models.authentication.{PsaId, PsaUser, PspId, PspUser}
import play.api.libs.json.Json
import uk.gov.hmrc.auth.core.AffinityGroup.{Individual, Organisation}

class StartNewTransferAuditModelSpec extends AnyFreeSpec with Matchers {

  "must create correct minimal json for Individual and PSA" in {

    val authenticatedUser = PsaUser(
      PsaId("21000005"),
      "internalId",
      None,
      Individual
    )

    val expectedJson = Json.obj(
      "journey"                   -> "startNewTransferReport",
      "internalReportReferenceId" -> "testID",
      "roleLoggedInAs"            -> "Psa",
      "affinityGroup"             -> "Individual",
      "requesterIdentifier"       -> "21000005"
    )

    val result = StartNewTransferAuditModel.build(authenticatedUser)
    result.auditType mustBe "OverseasPensionTransferReportStarted"
    result.detail mustBe expectedJson
  }

  "must create correct minimal json for Organisation and PSP" in {

    val authenticatedUser = PspUser(
      PspId("21000005"),
      "internalId",
      None,
      Organisation
    )

    val expectedJson = Json.obj(
      "journey"                   -> "startNewTransferReport",
      "internalReportReferenceId" -> "testID",
      "roleLoggedInAs"            -> "Psp",
      "affinityGroup"             -> "Organisation",
      "requesterIdentifier"       -> "21000005"
    )

    val result = StartNewTransferAuditModel.build(authenticatedUser)
    result.auditType mustBe "OverseasPensionTransferReportStarted"
    result.detail mustBe expectedJson
  }

  "must create correct json with pension scheme details" in {

    val pensionSchemeDetails = PensionSchemeDetails(
      SrnNumber("SRN123"),
      PstrNumber("PSTR123"),
      "Pension Scheme A"
    )

    val authenticatedUser = PspUser(
      PspId("21000005"),
      "internalId",
      Some(pensionSchemeDetails),
      Organisation
    )

    val expectedJson = Json.obj(
      "journey"                   -> "startNewTransferReport",
      "internalReportReferenceId" -> "testID",
      "pensionSchemeName"         -> "Pension Scheme A",
      "pensionSchemeTaxReference" -> "PSTR123",
      "roleLoggedInAs"            -> "Psp",
      "affinityGroup"             -> "Organisation",
      "requesterIdentifier"       -> "21000005"
    )

    val result = StartNewTransferAuditModel.build(authenticatedUser)
    result.auditType mustBe "OverseasPensionTransferReportStarted"
    result.detail mustBe expectedJson
  }
}

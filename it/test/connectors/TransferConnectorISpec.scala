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

package connectors

import base.BaseISpec
import connectors.parsers.TransferParser.GetAllTransfersType
import models.QtStatus.{InProgress, Submitted}
import models.{PstrNumber, QtNumber}
import models.responses._
import org.scalatest.OptionValues
import play.api.test.Injecting
import stubs.TransferBackendStub

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global

class TransferConnectorISpec extends BaseISpec with Injecting with OptionValues {

  private val connector: TransferConnector = inject[TransferConnector]

  private val pstr = PstrNumber("12345678AB")

  "TransferConnector.getAllTransfers" when {
    "the backend returns 200" must {
      "return Right(GetAllTransfersDTO) with the parsed transfers if payload valid" in {
        TransferBackendStub.getAllTransfersOk(pstr.value)

        val result: GetAllTransfersType = await(connector.getAllTransfers(pstr))

        result match {
          case Right(dto) =>
            dto.pstr.value         shouldBe pstr.value
            dto.transfers.nonEmpty shouldBe true
            val first = dto.transfers.head
            first.transferReference.value shouldBe "TR-001"
            first.qtReference.value       shouldBe QtNumber("QT564321")
            first.qtVersion.value         shouldBe "001"
            first.memberFirstName.value   shouldBe "David"
            first.memberSurname.value     shouldBe "Warne"
            first.nino.value              shouldBe "AA000000A"
            first.submissionDate.value    shouldBe Instant.parse("2025-03-14T00:00:00Z")
            first.lastUpdated             shouldBe empty
            first.qtStatus.value          shouldBe Submitted
            first.pstrNumber.value        shouldBe pstr
          case Left(err)  =>
            fail(s"Expected Right(dto) but got $err")
        }
      }
      "drop the invalid items and keep only valid ones if some of payload is invalid" in {
        // 3 items: 1 valid submitted, 1 valid in-progress, 1 invalid (has both dates)
        TransferBackendStub.getAllTransfersOkWithInvalidItems(pstr.value)

        val result = await(connector.getAllTransfers(pstr))

        result match {
          case Right(dto) =>
            dto.transfers.size                shouldBe 2
            all(dto.transfers.map(_.isValid)) shouldBe true
          case Left(err)  =>
            fail(s"Expected Right(dto) but got $err")
        }
      }
      "map to Left(AllTransfersUnexpectedError) if json malformed" in {
        TransferBackendStub.getAllTransfersMalformed(pstr.value)

        val result = await(connector.getAllTransfers(pstr))

        result match {
          case Left(_: AllTransfersUnexpectedError) => succeed
          case other                                => fail(s"Expected AllTransfersUnexpectedError but got $other")
        }
      }
    }

    "the backend returns 404" must {
      "map to Left(NoTransfersFound)" in {
        TransferBackendStub.getAllTransfersNotFound(pstr.value)

        val result = await(connector.getAllTransfers(pstr))

        result shouldBe Left(NoTransfersFound)
      }
    }

    "the backend returns 500" must {
      "map to Left(InternalServerError)" in {
        TransferBackendStub.getAllTransfersServerError(pstr.value)

        val result = await(connector.getAllTransfers(pstr))

        result shouldBe Left(InternalServerError)
      }
    }
  }

  "TransferConnector.getSpecificTransfer" when {

    val referenceId = "REF123"
    val now         = Instant.parse("2025-09-24T10:00:00Z")

    "the backend returns 200" must {

      "return Right(UserAnswersDTO) when qtStatus is InProgress and body is valid" in {
        TransferBackendStub.getSpecificTransferOk(
          referenceId    = referenceId,
          pstr           = pstr.value,
          qtStatus       = InProgress.toString,
          dataJson       = """{ "foo": "bar", "n": 1 }""",
          lastUpdatedIso = now.toString
        )

        val result = await(
          connector.getSpecificTransfer(
            transferReference = Some(referenceId),
            qtNumber          = None,
            pstrNumber        = pstr,
            qtStatus          = InProgress,
            versionNumber     = None
          )
        )

        result match {
          case Right(dto) =>
            dto.referenceId    shouldBe referenceId
            dto.pstr.value     shouldBe pstr.value
            dto.data.toString  should include ("foo")
            dto.lastUpdated    shouldBe now
          case Left(err)  =>
            fail(s"Expected Right(UserAnswersDTO) but got $err")
        }
      }

      "return Right(UserAnswersDTO) when qtStatus is Submitted and versionNumber provided" in {
        TransferBackendStub.getSpecificTransferOk(
          referenceId    = referenceId,
          pstr           = pstr.value,
          qtStatus       = Submitted.toString,
          dataJson       = """{ "submitted": true, "hasVersion": true }""",
          lastUpdatedIso = now.toString,
          versionNumber  = Some("002")
        )

        val result = await(
          connector.getSpecificTransfer(
            transferReference = Some(referenceId),
            qtNumber          = None,
            pstrNumber        = pstr,
            qtStatus          = Submitted,
            versionNumber     = Some("002")
          )
        )

        result match {
          case Right(dto) =>
            dto.referenceId   shouldBe referenceId
            dto.data.toString should include ("hasVersion")
          case Left(err)  =>
            fail(s"Expected Right(UserAnswersDTO) but got $err")
        }
      }

      "map to Left(UserAnswersErrorResponse) if body is malformed JSON" in {
        TransferBackendStub.getSpecificTransferMalformed(
          referenceId = referenceId,
          pstr        = pstr.value,
          qtStatus    = Submitted.toString
        )

        val result = await(
          connector.getSpecificTransfer(
            transferReference = Some(referenceId),
            qtNumber          = None,
            pstrNumber        = pstr,
            qtStatus          = Submitted,
            versionNumber     = None
          )
        )

        result match {
          case Left(_: UserAnswersErrorResponse) => succeed
          case other                              => fail(s"Expected UserAnswersErrorResponse but got $other")
        }
      }
    }

    "the backend returns 404" must {

      "map to Left(UserAnswersNotFoundResponse)" in {
        TransferBackendStub.getSpecificTransferNotFound(
          referenceId = referenceId,
          pstr        = pstr.value,
          qtStatus    = Submitted.toString
        )

        val result = await(
          connector.getSpecificTransfer(
            transferReference = Some(referenceId),
            qtNumber          = None,
            pstrNumber        = pstr,
            qtStatus          = Submitted,
            versionNumber     = None
          )
        )

        result shouldBe Left(UserAnswersNotFoundResponse)
      }
    }

    "the backend returns 500" must {

      "map to Left(UserAnswersErrorResponse)" in {
        TransferBackendStub.getSpecificTransferServerError(
          referenceId = referenceId,
          pstr        = pstr.value,
          qtStatus    = InProgress.toString
        )

        val result = await(
          connector.getSpecificTransfer(
            transferReference = Some(referenceId),
            qtNumber          = None,
            pstrNumber        = pstr,
            qtStatus          = InProgress,
            versionNumber     = None
          )
        )

        result match {
          case Left(_: UserAnswersErrorResponse) => succeed
          case other                             => fail(s"Expected Left(UserAnswersErrorResponse) but got $other")
        }
      }
    }
  }
}

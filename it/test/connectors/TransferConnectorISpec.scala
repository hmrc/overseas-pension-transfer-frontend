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

import base.ISpecBase
import connectors.parsers.TransferParser.GetAllTransfersType
import models.QtStatus.Submitted
import models.responses.*
import models.{PstrNumber, QtNumber}
import play.api.test.Helpers.*
import stubs.TransferBackendStub

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global

class TransferConnectorISpec extends ISpecBase {

  private val connector: TransferConnector = app.injector.instanceOf[TransferConnector]

  "TransferConnector.getAllTransfers" when {

    "the backend returns 200" must {
      "return Right(GetAllTransfersDTO) with the parsed transfers if payload valid" in {
        TransferBackendStub.stubGetAllTransfersOk(pstr.value, testNino)

        val result: GetAllTransfersType = await(connector.getAllTransfers(srn, pstr))

        result match {
          case Right(dto) =>
            dto.pstr.value mustBe pstr.value
            dto.transfers.nonEmpty mustBe true

            val first = dto.transfers.head
            first.transferId mustBe QtNumber("QT564321")
            first.qtVersion.value mustBe "001"
            first.memberFirstName.value mustBe "David"
            first.memberSurname.value mustBe "Warne"
            first.nino.value mustBe testNino
            first.submissionDate.value mustBe Instant.parse("2025-03-14T00:00:00Z")
            first.lastUpdated mustBe empty
            first.qtStatus.value mustBe Submitted
            first.pstrNumber.value mustBe pstr

          case Left(err) =>
            fail(s"Expected Right(dto) but got $err")
        }
      }

      "drop the invalid items and keep only valid ones if some of payload is invalid" in {
        // 3 items: 1 valid submitted, 1 valid in-progress, 1 invalid (has both dates)
        TransferBackendStub.stubGetAllTransfersOkWithInvalidItems(pstr.value, testNino)

        val result = await(connector.getAllTransfers(srn, pstr))

        result match {
          case Right(dto) =>
            dto.transfers.size mustBe 2
            all(dto.transfers.map(_.isValid)) mustBe true
            all(dto.transfers.map(_.nino.value)) mustBe testNino

          case Left(err) =>
            fail(s"Expected Right(dto) but got $err")
        }
      }

      "map to Left(AllTransfersUnexpectedError) if json malformed" in {
        TransferBackendStub.stubGetAllTransfersMalformed(pstr.value)

        val result = await(connector.getAllTransfers(srn, pstr))

        result match {
          case Left(_: AllTransfersUnexpectedError) => succeed
          case other                                => fail(s"Expected AllTransfersUnexpectedError but got $other")
        }
      }
    }

    "the backend returns 404" must {
      "map to Left(NoTransfersFound)" in {
        TransferBackendStub.stubGetAllTransfersNotFound(pstr.value)

        val result = await(connector.getAllTransfers(srn, pstr))

        result mustBe Left(NoTransfersFound)
      }
    }

    "the backend returns 500" must {
      "map to Left(InternalServerError)" in {
        TransferBackendStub.stubGetAllTransfersServerError(pstr.value)

        val result = await(connector.getAllTransfers(srn, pstr))

        result mustBe Left(InternalServerError)
      }
    }
  }
}

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

package services

import base.SpecBase
import connectors.TransferConnector
import models.dtos.GetAllTransfersDTO
import models.responses.{AllTransfersUnexpectedError, NoTransfersFound, TransferError}
import models.{AllTransfersItem, DashboardData, PstrNumber}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import queries.dashboard.{TransfersDataUpdatedAtQuery, TransfersOverviewQuery, TransfersSyncedAtQuery}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class TransferServiceSpec extends AnyFreeSpec with SpecBase with Matchers with MockitoSugar {

  implicit private val hc: HeaderCarrier = HeaderCarrier()

  "TransferService.getAllTransfersData" - {

    "must clear transfers and set syncedAt when connector returns NoTransfersFound, and must NOT change dataUpdatedAt" in {

      val mockConnector = mock[TransferConnector]
      val service       = new TransferService(mockConnector)

      val pstr = PstrNumber("12345678AB")

      val existingDataUpdatedAt = Instant.parse("2025-09-01T12:00:00Z")
      val startDd               = DashboardData(id = "id")
        .set(TransfersDataUpdatedAtQuery, existingDataUpdatedAt).success.value
        .set(TransfersOverviewQuery, Seq(AllTransfersItem(userAnswersTransferNumber, None, None, None, None, None, None, None, None, None))).success.value

      when(mockConnector.getAllTransfers(meq(pstr))(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(Left(NoTransfersFound)))

      val result = await(service.getAllTransfersData(startDd, pstr))

      result.isRight mustBe true
      val updated = result.toOption.get

      updated.get(TransfersOverviewQuery) mustBe Some(Seq.empty)
      updated.get(TransfersSyncedAtQuery).isDefined mustBe true
      updated.get(TransfersDataUpdatedAtQuery) mustBe Some(existingDataUpdatedAt)
    }

    "must merge transfers and set both syncedAt (now) and dataUpdatedAt (from DTO) when connector returns Right(dto)" in {

      val mockConnector = mock[TransferConnector]
      val service       = new TransferService(mockConnector)

      val pstr = PstrNumber("12345678AB")

      val dtoTransfers: Seq[AllTransfersItem] = Seq(
        AllTransfersItem(
          transferId      = userAnswersTransferNumber,
          qtVersion       = None,
          nino            = None,
          memberFirstName = Some("Ada"),
          memberSurname   = Some("Lovelace"),
          submissionDate  = None,
          lastUpdated     = None,
          qtStatus        = None,
          pstrNumber      = None,
          qtDate          = None
        )
      )
      val dtoLastUpdated                      = Instant.parse("2025-09-24T08:00:00Z")
      val dto                                 = GetAllTransfersDTO(pstr, dtoLastUpdated, dtoTransfers)

      when(mockConnector.getAllTransfers(meq(pstr))(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(Right(dto)))

      val startDd = DashboardData(id = "id")

      val result = await(service.getAllTransfersData(startDd, pstr))

      result.isRight mustBe true
      val updated = result.toOption.get

      updated.get(TransfersOverviewQuery) mustBe Some(dtoTransfers)
      updated.get(TransfersSyncedAtQuery).isDefined mustBe true
      updated.get(TransfersDataUpdatedAtQuery) mustBe Some(dtoLastUpdated)
    }

    "must remove transfers with missing first name or surname" in {

      val mockConnector = mock[TransferConnector]
      val service       = new TransferService(mockConnector)

      val pstr = PstrNumber("12345678AB")

      val rawTransfers: Seq[AllTransfersItem] = Seq(
        AllTransfersItem(userAnswersTransferNumber, None, None, None, Some("Ada"), Some("Lovelace"), None, None, None, None),
        AllTransfersItem(userAnswersTransferNumber, None, None, None, Some(""), Some("Valid"), None, None, None, None), // invalid
        AllTransfersItem(userAnswersTransferNumber, None, None, None, Some("Grace"), None, None, None, None, None) // invalid
      )

      val dtoLastUpdated = Instant.parse("2025-10-01T09:00:00Z")
      val dto            = GetAllTransfersDTO(pstr, dtoLastUpdated, rawTransfers)

      when(mockConnector.getAllTransfers(meq(pstr))(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(Right(dto)))

      val startDd = DashboardData(id = "id")

      val result = await(service.getAllTransfersData(startDd, pstr))

      result.isRight mustBe true
      val updated = result.toOption.get

      updated.get(TransfersOverviewQuery) mustBe Some(Seq(rawTransfers.head))
    }

    "must pass through connector errors unchanged" in {

      val mockConnector = mock[TransferConnector]
      val service       = new TransferService(mockConnector)

      val pstr = PstrNumber("12345678AB")

      val err: TransferError = AllTransfersUnexpectedError("boom", None)

      when(mockConnector.getAllTransfers(meq(pstr))(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(Left(err)))

      val startDd = DashboardData(id = "id")

      val result = await(service.getAllTransfersData(startDd, pstr))

      result mustBe Left(err)
    }
  }
}

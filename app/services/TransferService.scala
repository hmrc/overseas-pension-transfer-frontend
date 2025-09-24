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

import connectors.TransferConnector
import models.responses.{AllTransfersUnexpectedError, NoTransfersFound, TransferError}
import models.{DashboardData, PstrNumber}
import queries.dashboard.{TransfersDataUpdatedAtQuery, TransfersOverviewQuery, TransfersSyncedAtQuery}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TransferService @Inject() (
    connector: TransferConnector
  )(implicit ec: ExecutionContext
  ) {

  def getAllTransfersData(current: DashboardData, pstr: PstrNumber)(implicit hc: HeaderCarrier): Future[Either[TransferError, DashboardData]] =
    connector.getAllTransfers(pstr).map {
      case Left(NoTransfersFound) =>
        (for {
          dd1 <- current.set(TransfersOverviewQuery, Seq.empty)
          dd2 <- dd1.set(TransfersSyncedAtQuery, Instant.now())
        } yield dd2).fold(
          e => Left(AllTransfersUnexpectedError("Failed to update dashboard", Some(e.getMessage))),
          updated => Right(updated)
        )
      case Left(err)              =>
        Left(err)
      case Right(dto)             =>
        (for {
          dd1 <- current.set(TransfersOverviewQuery, dto.transfers)
          dd2 <- dd1.set(TransfersSyncedAtQuery, Instant.now())
          dd3 <- dd2.set(TransfersDataUpdatedAtQuery, dto.lastUpdated) // upstream timestamp
        } yield dd3).fold(
          e => Left(AllTransfersUnexpectedError("Failed to merge transfers", Some(e.getMessage))),
          updated => Right(updated)
        )
    }
}

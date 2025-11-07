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

import models.audit.JourneyStartedType.StartJourneyFailed
import models.audit.{JourneyStartedType, ReportStartedAuditModel}
import models.authentication.AuthenticatedUser
import models.{AllTransfersItem, PensionSchemeDetails, TransferId}
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.lock.LockRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LockService @Inject() (
    lockRepository: LockRepository,
    auditService: AuditService
  )(implicit ec: ExecutionContext
  ) extends Logging {

  def takeLockWithAudit(
      transferId: TransferId,
      owner: String,
      ttlSeconds: Long,
      authenticatedUser: AuthenticatedUser,
      schemeDetails: PensionSchemeDetails,
      journeyType: JourneyStartedType,
      allTransfersItem: Option[AllTransfersItem]
    )(implicit hc: HeaderCarrier
    ): Future[Boolean] = {

    lockRepository.takeLock(transferId.value, owner, ttlSeconds.seconds).flatMap {
      case Some(_) =>
        logger.info(s"[LockService] Lock acquired for ${transferId.value} by $owner")
        auditService.audit(
          ReportStartedAuditModel.build(
            transferId,
            authenticatedUser,
            schemeDetails,
            journeyType,
            allTransfersItem,
            None
          )
        )
        Future.successful(true)

      case None =>
        logger.warn(s"[LockService] Lock already taken for ${transferId.value}")
        auditService.audit(
          ReportStartedAuditModel.build(
            transferId,
            authenticatedUser,
            schemeDetails,
            StartJourneyFailed,
            allTransfersItem,
            Some("Transfer is locked by someone else")
          )
        )
        Future.successful(false)
    }
  }

  def takeLock(lockId: String, owner: String, ttlSeconds: Long): Future[Boolean] =
    lockRepository.takeLock(lockId, owner, ttlSeconds.seconds).map(_.isDefined)

  def releaseLock(lockId: String, owner: String): Future[Unit] =
    lockRepository.releaseLock(lockId, owner)

}

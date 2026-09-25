/*
 * Copyright 2026 HM Revenue & Customs
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

package repositories

import config.FrontendAppConfig
import org.mongodb.scala.model.Filters.{and, equal, gt, gte, lte}
import org.mongodb.scala.model.{IndexModel, IndexOptions, Indexes, Updates}
import play.api.Logger
import uk.gov.hmrc.mongo.{MongoComponent, TimestampSupport}
import uk.gov.hmrc.mongo.MongoUtils.DuplicateKey
import uk.gov.hmrc.mongo.lock.Lock
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.time.{Duration as JavaDuration, Instant}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ExpiringMongoLockRepository @Inject() (
  mongoComponent: MongoComponent,
  timestampSupport: TimestampSupport,
  appConfig: FrontendAppConfig
)(implicit
  ec: ExecutionContext
) extends PlayMongoRepository[Lock](
      mongoComponent,
      collectionName = "locks",
      domainFormat = Lock.format,
      indexes = Seq(
        IndexModel(
          Indexes.ascending("expiryTime"),
          IndexOptions()
            .name("expiryTimeIdx")
            .expireAfter(appConfig.dashboardLockTtl, TimeUnit.SECONDS)
        )
      ),
      replaceIndexes = true
    ) {

  private val logger         = Logger(getClass)
  private val lockTtlSeconds = appConfig.dashboardLockTtl

  def takeLock(lockId: String, owner: String): Future[Option[Lock]] = {
    val now        = timestampSupport.timestamp()
    val expiryTime = now.plus(JavaDuration.ofSeconds(lockTtlSeconds))

    (for {
      deleteResult <- collection
                        .deleteOne(
                          and(
                            equal(Lock.id, lockId),
                            lte(Lock.expiryTime, now)
                          )
                        )
                        .toFuture()
      _             = if (deleteResult.getDeletedCount != 0)
                        logger.info(s"Removed ${deleteResult.getDeletedCount} expired locks for $lockId")
      lock          = Lock(
                        id = lockId,
                        owner = owner,
                        timeCreated = now,
                        expiryTime = expiryTime
                      )
      _            <- collection
                        .insertOne(lock)
                        .toFuture()
      _             = logger.debug(s"Took lock '$lockId' for '$owner' at $now. Expires at: $expiryTime")
    } yield Some(lock)).recover { case DuplicateKey(_) =>
      logger.debug(s"Unable to take lock '$lockId' for '$owner'")
      None
    }
  }

  def releaseLock(lockId: String, owner: String): Future[Unit] = {
    logger.debug(s"Releasing lock '$lockId' for '$owner'")
    collection
      .deleteOne(
        and(
          equal(Lock.id, lockId),
          equal(Lock.owner, owner)
        )
      )
      .toFuture()
      .map(_ => ())
  }

  def disownLock(lockId: String, owner: String, updatedExpiry: Option[Instant] = None): Future[Unit] = {
    logger.debug(
      s"Disowning lock '$lockId'" + updatedExpiry.map(exp => s" and setting expiryTime to '$exp'").getOrElse("")
    )
    val expiryUpdate = updatedExpiry.map(exp => Updates.set(Lock.expiryTime, exp)).toSeq
    collection
      .findOneAndUpdate(
        filter = and(
          equal(Lock.id, lockId),
          equal(Lock.owner, owner)
        ),
        update = Seq(
          Updates.set(Lock.owner, s"$owner (disowned)")
        ) ++ expiryUpdate
      )
      .toFuture()
      .map(_ => ())
  }

  def refreshExpiry(lockId: String, owner: String): Future[Boolean] = {
    val timeCreated = timestampSupport.timestamp()
    val expiryTime  = timeCreated.plus(JavaDuration.ofSeconds(lockTtlSeconds))

    // Use findOneAndUpdate to ensure the read and the write are performed as one atomic operation
    collection
      .findOneAndUpdate(
        filter = and(
          equal(Lock.id, lockId),
          equal(Lock.owner, owner),
          gte(Lock.expiryTime, timeCreated)
        ),
        update = Updates.set(Lock.expiryTime, expiryTime)
      )
      .toFutureOption()
      .map {
        case Some(_) =>
          logger.debug(s"Renewed lock '$lockId' for '$owner' at $timeCreated.  Expires at: $expiryTime")
          true
        case None    =>
          logger.debug(s"Could not renew lock '$lockId' for '$owner' that does not exist or has expired")
          false
      }
      .recover { case DuplicateKey(_) =>
        logger.debug(s"Unable to renew lock '$lockId' for '$owner'")
        false
      }
  }

  def isLocked(lockId: String, owner: String): Future[Boolean] =
    collection
      .find(
        and(
          equal(Lock.id, lockId),
          equal(Lock.owner, owner),
          gt(Lock.expiryTime, timestampSupport.timestamp())
        )
      )
      .toFuture()
      .map(_.nonEmpty)
}

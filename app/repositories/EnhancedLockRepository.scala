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

import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.TimestampSupport
import org.mongodb.scala.result.DeleteResult
import uk.gov.hmrc.mongo.lock.Lock
import uk.gov.hmrc.mongo.lock.MongoLockRepository
import org.mongodb.scala.model.Filters.lte

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnhancedLockRepository @Inject() (
  mongoComponent: MongoComponent,
  timestampSupport: TimestampSupport
)(implicit ec: ExecutionContext)
    extends MongoLockRepository(mongoComponent, timestampSupport) {

  def removeAllExpiredLocks(): Future[DeleteResult] = {
    val now: Instant = timestampSupport.timestamp()

    collection
      .deleteMany(
        lte(Lock.expiryTime, now)
      )
      .toFuture()
  }

}

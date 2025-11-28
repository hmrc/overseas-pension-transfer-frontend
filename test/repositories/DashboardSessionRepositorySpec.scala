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

package repositories

import base.SpecBase
import config.TestAppConfig
import models.{AllTransfersItem, DashboardData, QtNumber, QtStatus}
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import services.EncryptionService
import uk.gov.hmrc.mongo.test.CleanMongoCollectionSupport

import java.time.{Duration, _}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class DashboardSessionRepositorySpec
    extends AnyFreeSpec
    with Matchers
    with OptionValues
    with CleanMongoCollectionSupport
    with ScalaFutures
    with SpecBase {

  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = 5.seconds, interval = 50.millis)

  override val databaseName: String = "test-dashboard"

  override val now: Instant = Instant.parse("2025-01-01T00:00:00Z")
  private val fixedClock    = Clock.fixed(now, ZoneOffset.UTC)
  private val encryption    = new EncryptionService("test-master-key")
  private val appConfig     = new TestAppConfig

  private val repository = new DashboardSessionRepository(
    mongoComponent    = mongoComponent,
    encryptionService = encryption,
    appConfig         = appConfig,
    clock             = fixedClock
  )

  "DashboardSessionRepository" - {

    "must save and retrieve DashboardData with encryption" in {
      val dashboard = DashboardData("id-1", data = DashboardData.empty.data, lastUpdated = now)
      repository.set(dashboard).futureValue mustBe true

      val retrieved = repository.get("id-1").futureValue.value
      retrieved.id mustBe "id-1"
      retrieved.lastUpdated mustBe now
    }

    "must update lastUpdated when keepAlive is called" in {
      val dashboard = DashboardData("id-keepalive", data = DashboardData.empty.data, lastUpdated = now.minusSeconds(1000))
      repository.set(dashboard).futureValue mustBe true

      repository.keepAlive("id-keepalive").futureValue mustBe true

      val updated = repository.get("id-keepalive").futureValue.value
      updated.lastUpdated mustBe now
    }

    "must delete DashboardData when clear is called" in {
      val dashboard = DashboardData("id-clear", data = DashboardData.empty.data, lastUpdated = now)
      repository.set(dashboard).futureValue mustBe true

      repository.clear("id-clear").futureValue mustBe true
      repository.get("id-clear").futureValue mustBe None
    }

    "must find expiring transfers within 2 days" in {
      def makeTransfer(status: Option[QtStatus], lastUpdated: Option[Instant]) =
        AllTransfersItem(
          transferId      = QtNumber("QT123456"),
          qtVersion       = None,
          qtStatus        = status,
          nino            = None,
          memberFirstName = None,
          memberSurname   = None,
          qtDate          = None,
          lastUpdated     = lastUpdated,
          pstrNumber      = None,
          submissionDate  = None
        )

      val inProgress   = makeTransfer(Some(QtStatus.InProgress), Some(now.minus(Period.ofDays(24))))
      val amendInProg  = makeTransfer(Some(QtStatus.AmendInProgress), Some(now.minus(Period.ofDays(23)).minus(Duration.ofHours(23))))
      val oldTransfer  = makeTransfer(Some(QtStatus.InProgress), Some(now.minus(Period.ofDays(10))))
      val complete     = makeTransfer(Some(QtStatus.Compiled), Some(now.minus(Period.ofDays(1))))
      val allTransfers = Seq(inProgress, amendInProg, oldTransfer, complete)
      val expiring     = repository.findExpiringWithin2Days(allTransfers)

      expiring must contain(inProgress)
      expiring must contain(amendInProg)
      expiring must not contain oldTransfer
      expiring must not contain complete
    }

    "must handle missing DashboardData gracefully" in {
      repository.get("non-existent").futureValue mustBe None
    }

    "must handle empty ID gracefully" in {
      val dashboard = DashboardData("", data = DashboardData.empty.data, lastUpdated = now)
      repository.set(dashboard).futureValue mustBe true
      repository.get("").futureValue.value.id mustBe ""
      repository.clear("").futureValue mustBe true
      repository.get("").futureValue mustBe None
    }
  }
}

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
import models.SessionData
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json
import services.EncryptionService
import uk.gov.hmrc.mongo.test.CleanMongoCollectionSupport

import java.time.{Clock, Instant, ZoneOffset}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class SessionRepositorySpec
    extends AnyFreeSpec
    with Matchers
    with OptionValues
    with CleanMongoCollectionSupport
    with ScalaFutures
    with SpecBase {

  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = 5.seconds, interval = 50.millis)

  override val databaseName: String = "test-session"

  private val now        = Instant.parse("2025-01-01T00:00:00Z")
  private val fixedClock = Clock.fixed(now, ZoneOffset.UTC)
  private val encryption = new EncryptionService("test-master-key")
  private val appConfig  = new TestAppConfig

  private val repository = new SessionRepository(
    mongoComponent    = mongoComponent,
    encryptionService = encryption,
    appConfig         = appConfig,
    clock             = fixedClock
  )

  private def sessionData(sessionId: String, transferId: String) =
    SessionData(
      sessionId         = sessionId,
      transferId        = transferId,
      schemeInformation = schemeDetails,
      user              = psaUser.updatePensionSchemeDetails(schemeDetails),
      data              = Json.obj(),
      lastUpdated       = now
    )

  "SessionRepository" - {

    "must save and retrieve SessionData with encryption" in {
      val session = sessionData("session-1", "transfer-1")
      repository.set(session).futureValue mustBe true

      val retrieved = repository.get("session-1").futureValue.value
      retrieved.sessionId mustBe "session-1"
      retrieved.transferId mustBe "transfer-1"
      retrieved.lastUpdated mustBe now
    }

    "must update lastUpdated when keepAlive is called" in {
      val session = sessionData("session-keepalive", "transfer-keepalive").copy(lastUpdated = now.minusSeconds(1000))
      repository.set(session).futureValue mustBe true

      repository.keepAlive("session-keepalive").futureValue mustBe true

      val updated = repository.get("session-keepalive").futureValue.value
      updated.lastUpdated mustBe now
    }

    "must update lastUpdated when keepAliveByTransferId is called" in {
      val session = sessionData("session-transfer", "transfer-123").copy(lastUpdated = now.minusSeconds(500))
      repository.set(session).futureValue mustBe true

      repository.keepAliveByTransferId("transfer-123").futureValue mustBe true

      val updated = repository.getByTransferId("transfer-123").futureValue.value
      updated.lastUpdated mustBe now
    }

    "must delete SessionData when clear(id) is called" in {
      val session = sessionData("session-clear", "transfer-clear")
      repository.set(session).futureValue mustBe true

      repository.clear("session-clear").futureValue mustBe true
      repository.get("session-clear").futureValue mustBe None
    }

    "must delete all SessionData when clear() is called" in {
      val session1 = sessionData("session-1", "transfer-1")
      val session2 = sessionData("session-2", "transfer-2")
      repository.set(session1).futureValue mustBe true
      repository.set(session2).futureValue mustBe true

      repository.clear("session-1").futureValue mustBe true
      repository.clear("session-2").futureValue mustBe true
      repository.get("session-1").futureValue mustBe None
      repository.get("session-2").futureValue mustBe None
    }

    "must handle missing SessionData gracefully" in {
      repository.get("non-existent").futureValue mustBe None
      repository.getByTransferId("non-existent").futureValue mustBe None
    }
  }
}

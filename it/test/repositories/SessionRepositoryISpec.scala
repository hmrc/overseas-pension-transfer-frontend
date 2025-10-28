/*
 * Copyright 2024 HM Revenue & Customs
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
import config.FrontendAppConfig
import models.authentication.{PsaId, PsaUser}
import models.{PensionSchemeDetails, PstrNumber, SessionData, SrnNumber}
import org.mockito.Mockito.when
import org.mongodb.scala.model.Filters
import org.scalactic.source.Position
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.slf4j.MDC
import play.api.libs.json.Json
import services.EncryptionService
import uk.gov.hmrc.auth.core.AffinityGroup.Individual
import uk.gov.hmrc.mdc.MdcExecutionContext
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}
import scala.concurrent.{ExecutionContext, Future}

class SessionRepositoryISpec
    extends AnyFreeSpec
    with Matchers
    with DefaultPlayMongoRepositorySupport[SessionData]
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with MockitoSugar
    with SpecBase {

  private val instant          = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)

  private val sessionData = SessionData(
    "id",
    userAnswersTransferNumber,
    PensionSchemeDetails(
      SrnNumber("1234567890123"),
      PstrNumber("12345678AB"),
      "Scheme Name"
    ),
    PsaUser(
      PsaId("A123456"),
      "internalId",
      None,
      Individual
    ),
    Json.obj("foo" -> "bar"),
    Instant.ofEpochSecond(1)
  )

  private val mockAppConfig = mock[FrontendAppConfig]
  when(mockAppConfig.cacheTtl) thenReturn 1L

  implicit val productionLikeTestMdcExecutionContext: ExecutionContext = MdcExecutionContext()

  private val encryptionService = new EncryptionService("test-master-key")

  override protected val repository: SessionRepository = new SessionRepository(
    mongoComponent    = mongoComponent,
    encryptionService = encryptionService,
    appConfig         = mockAppConfig,
    clock             = stubClock
  )

  ".set" - {

    "must set the last updated time on the supplied user answers to `now`, and save them" in {

      val expectedResult = sessionData copy (lastUpdated = instant)

      repository.set(sessionData).futureValue
      val updatedRecord = find(Filters.equal("_id", sessionData.sessionId)).futureValue.headOption.value

      updatedRecord mustEqual expectedResult
    }

    mustPreserveMdc(repository.set(sessionData))
  }

  ".get" - {

    "when there is a record for this id" - {

      "must update the lastUpdated time and get the record" in {

        insert(sessionData).futureValue

        val result         = repository.get(sessionData.sessionId).futureValue
        val expectedResult = sessionData copy (lastUpdated = instant)

        result.value mustEqual expectedResult
      }
    }

    "when there is no record for this id" - {

      "must return None" in {

        repository.get("id that does not exist").futureValue must not be defined
      }
    }

    mustPreserveMdc(repository.get(sessionData.transferId.value))
  }

  ".getByTransferId" - {

    "when there is a record for this id" - {

      "must update the lastUpdated time and get the record" in {

        insert(sessionData).futureValue

        val result         = repository.getByTransferId(sessionData.transferId.value).futureValue
        val expectedResult = sessionData copy (lastUpdated = instant)

        result.value mustEqual expectedResult
      }
    }

    "when there is no record for this id" - {

      "must return None" in {

        repository.getByTransferId("id that does not exist").futureValue must not be defined
      }
    }

    mustPreserveMdc(repository.get(sessionData.transferId.value))
  }

  ".clear" - {

    "must remove a record" in {

      insert(sessionData).futureValue

      repository.clear(sessionData.transferId.value).futureValue

      repository.get(sessionData.transferId.value).futureValue must not be defined
    }

    "must return true when there is no record to remove" in {
      val result = repository.clear("id that does not exist").futureValue

      result mustEqual true
    }

    mustPreserveMdc(repository.clear(sessionData.transferId.value))
  }

  ".keepAlive" - {

    "when there is a record for this id" - {

      "must update its lastUpdated to `now` and return true" in {

        insert(sessionData).futureValue

        val result = repository.keepAlive(sessionData.sessionId).futureValue

        val expectedUpdatedAnswers = sessionData copy (lastUpdated = instant)

        val updatedAnswers = find(Filters.equal("_id", sessionData.sessionId)).futureValue.headOption.value
        updatedAnswers mustEqual expectedUpdatedAnswers
      }
    }

    "when there is no record for this id" - {

      "must return true" in {

        repository.keepAlive("id that does not exist").futureValue mustEqual true
      }
    }

    mustPreserveMdc(repository.keepAlive(sessionData.transferId.value))
  }

  ".keepAliveByTransferId" - {

    "when there is a record for this id" - {

      "must update its lastUpdated to `now` and return true" in {

        insert(sessionData).futureValue

        val result = repository.keepAliveByTransferId(sessionData.transferId.value).futureValue

        val expectedUpdatedAnswers = sessionData copy (lastUpdated = instant)

        val updatedAnswers = find(Filters.equal("transferId", sessionData.transferId.value)).futureValue.headOption.value
        updatedAnswers mustEqual expectedUpdatedAnswers
      }
    }

    "when there is no record for this id" - {

      "must return true" in {

        repository.keepAlive("id that does not exist").futureValue mustEqual true
      }
    }

    mustPreserveMdc(repository.keepAlive(sessionData.transferId.value))
  }

  "EncryptionService" - {
    "must correctly encrypt and decrypt data" in {
      val service   = new EncryptionService("encryption-test-key")
      val plainText = "sensitive data 123"

      val encrypted = service.encrypt(plainText)
      encrypted must not be plainText

      val decrypted = service.decrypt(encrypted)
      decrypted mustEqual Right(plainText)
    }

    "must return Left when decrypting invalid cipher text" in {
      val service       = new EncryptionService("another-key")
      val invalidCipher = "invalid-data-123"
      val result        = service.decrypt(invalidCipher)
      result.isLeft mustBe true
    }
  }

  private def mustPreserveMdc[A](f: => Future[A])(implicit pos: Position): Unit =
    "must preserve MDC" in {

      MDC.put("test", "foo")

      f.map { _ =>
        Option(MDC.get("test"))
      }.futureValue mustEqual Some("foo")
    }
}

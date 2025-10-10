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

import config.FrontendAppConfig
import models.authentication.{PsaId, PsaUser}
import models.{PensionSchemeDetails, PstrNumber, SessionData, SrnNumber, UserAnswers}
import org.mockito.Mockito.when
import org.mongodb.scala.model.Filters
import org.scalactic.source.Position
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.slf4j.MDC
import uk.gov.hmrc.mdc.MdcExecutionContext
import play.api.libs.json.Json
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.{Clock, Instant, ZoneId}
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future}

class SessionRepositoryISpec
    extends AnyFreeSpec
    with Matchers
    with DefaultPlayMongoRepositorySupport[SessionData]
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with MockitoSugar {

  private val instant          = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)

  private val sessionData = SessionData(
    "id",
    "transferId",
    PensionSchemeDetails(
      SrnNumber("1234567890123"),
      PstrNumber("12345678AB"),
      "Scheme Name"
    ),
    PsaUser(
      PsaId("A123456"),
      "internalId",
      None
    ),
    Json.obj("foo" -> "bar"),
    Instant.ofEpochSecond(1)
  )

  private val mockAppConfig = mock[FrontendAppConfig]
  when(mockAppConfig.cacheTtl) thenReturn 1L

  implicit val productionLikeTestMdcExecutionContext: ExecutionContext = MdcExecutionContext()

  override protected val repository: SessionRepository = new SessionRepository(
    mongoComponent = mongoComponent,
    appConfig      = mockAppConfig,
    clock          = stubClock
  )

  ".set" - {

    "must set the last updated time on the supplied user answers to `now`, and save them" in {

      val expectedResult = sessionData copy (lastUpdated = instant)

      val setResult     = repository.set(sessionData).futureValue
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

    mustPreserveMdc(repository.get(sessionData.transferId))
  }

  ".getByTransferId" - {

    "when there is a record for this id" - {

      "must update the lastUpdated time and get the record" in {

        insert(sessionData).futureValue

        val result         = repository.getByTransferId(sessionData.transferId).futureValue
        val expectedResult = sessionData copy (lastUpdated = instant)

        result.value mustEqual expectedResult
      }
    }

    "when there is no record for this id" - {

      "must return None" in {

        repository.getByTransferId("id that does not exist").futureValue must not be defined
      }
    }

    mustPreserveMdc(repository.get(sessionData.transferId))
  }

  ".clear" - {

    "must remove a record" in {

      insert(sessionData).futureValue

      val result = repository.clear(sessionData.transferId).futureValue

      repository.get(sessionData.transferId).futureValue must not be defined
    }

    "must return true when there is no record to remove" in {
      val result = repository.clear("id that does not exist").futureValue

      result mustEqual true
    }

    mustPreserveMdc(repository.clear(sessionData.transferId))
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

    mustPreserveMdc(repository.keepAlive(sessionData.transferId))
  }

  ".keepAliveByTransferId" - {

    "when there is a record for this id" - {

      "must update its lastUpdated to `now` and return true" in {

        insert(sessionData).futureValue

        val result = repository.keepAliveByTransferId(sessionData.transferId).futureValue

        val expectedUpdatedAnswers = sessionData copy (lastUpdated = instant)

        val updatedAnswers = find(Filters.equal("transferId", sessionData.transferId)).futureValue.headOption.value
        updatedAnswers mustEqual expectedUpdatedAnswers
      }
    }

    "when there is no record for this id" - {

      "must return true" in {

        repository.keepAlive("id that does not exist").futureValue mustEqual true
      }
    }

    mustPreserveMdc(repository.keepAlive(sessionData.transferId))
  }

  private def mustPreserveMdc[A](f: => Future[A])(implicit pos: Position): Unit =
    "must preserve MDC" in {

      MDC.put("test", "foo")

      f.map { _ =>
        Option(MDC.get("test"))
      }.futureValue mustEqual Some("foo")
    }
}

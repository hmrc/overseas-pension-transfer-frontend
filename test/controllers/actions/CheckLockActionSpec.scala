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

package controllers.actions

import base.SpecBase
import models.requests.SchemeRequest
import models.{PensionSchemeDetails, PstrNumber, SessionData, SrnNumber}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.Inside
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.SEE_OTHER
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import repositories.{ExpiringMongoLockRepository, SessionRepository}
import uk.gov.hmrc.mongo.lock.Lock

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckLockActionSpec extends AnyFreeSpec with SpecBase with MockitoSugar with Inside {

  private val sessionData = SessionData(
    "sessionId",
    userAnswersTransferNumber,
    PensionSchemeDetails(
      SrnNumber("12345"),
      PstrNumber("12345678AB"),
      "Scheme Name"
    ),
    psaUser,
    Json.obj(),
    now
  )

  private val lock = Lock(userAnswersTransferNumber.value, psaId.value, Instant.now(), Instant.now().plusSeconds(900))

  private val request = SchemeRequest(FakeRequest(), psaUser, schemeDetails)

  class Harness(sessionRepository: SessionRepository, lockRepository: ExpiringMongoLockRepository)
      extends CheckLockActionImpl(sessionRepository, lockRepository) {
    def callRefine[A](request: SchemeRequest[A]): Future[Either[Result, SchemeRequest[A]]] = refine(request)
  }

  "Check Lock Action" - {

    "when there is no session data in the cache" - {
      "must redirect to JourneyRecovery" in {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))

        val action = new Harness(mockSessionRepository, mockMongoLockRepository)

        val result = action.callRefine(request).futureValue

        inside(result) { case Left(r) =>
          r.header.status mustBe SEE_OTHER
          r.header.headers.get("Location") mustBe Some(
            controllers.routes.JourneyRecoveryController.onPageLoad().url
          )
        }
      }

    }

    "when there is session data in the cache" - {
      "and the lock could be refreshed" - {
        "then the request will be returned so the next action can run" in {
          when(mockSessionRepository.get("id")) thenReturn Future(Some(sessionData))

          when(mockMongoLockRepository.refreshExpiry(userAnswersTransferNumber.value, psaId.value))
            .thenReturn(Future.successful(true))

          val action = new Harness(mockSessionRepository, mockMongoLockRepository)

          val result = action.callRefine(request).futureValue

          inside(result) { case Right(r) =>
            r mustBe request
          }
        }
      }

      "and the lock couldn't be refreshed" - {
        "and the lock could be taken" - {
          "then the request will be returned so the next action can run" in {
            when(mockSessionRepository.get("id")) thenReturn Future(Some(sessionData))

            when(mockMongoLockRepository.refreshExpiry(userAnswersTransferNumber.value, psaId.value))
              .thenReturn(Future.successful(false))

            when(mockMongoLockRepository.takeLock(userAnswersTransferNumber.value, psaId.value))
              .thenReturn(Future.successful(Some(lock)))

            val action = new Harness(mockSessionRepository, mockMongoLockRepository)

            val result = action.callRefine(request).futureValue

            inside(result) { case Right(r) =>
              r mustBe request
            }
          }
        }

        "and the lock couldn't be taken" - {
          "must redirect to JourneyRecovery" in {
            when(mockSessionRepository.get("id")) thenReturn Future(Some(sessionData))

            when(mockMongoLockRepository.refreshExpiry(userAnswersTransferNumber.value, psaId.value))
              .thenReturn(Future.successful(false))

            when(mockMongoLockRepository.takeLock(userAnswersTransferNumber.value, psaId.value))
              .thenReturn(Future.successful(None))

            val action = new Harness(mockSessionRepository, mockMongoLockRepository)

            val result = action.callRefine(SchemeRequest(FakeRequest(), psaUser, schemeDetails)).futureValue

            inside(result) { case Left(r) =>
              r.header.status mustBe SEE_OTHER
              r.header.headers.get("Location") mustBe Some(
                controllers.routes.JourneyRecoveryController.onPageLoad().url
              )
            }
          }
        }
      }
    }
  }
}

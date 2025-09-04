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
import models.UserAnswers
import models.requests.{DataRequest, IdentifierRequest}
import org.mockito.Mockito._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.SEE_OTHER
import play.api.mvc.Result
import play.api.test.FakeRequest
import repositories.SessionRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataRetrievalActionSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  class Harness(sessionRepository: SessionRepository) extends DataRetrievalActionImpl(sessionRepository) {
    def callRefine[A](request: IdentifierRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  "Data Retrieval Action" - {

    "when there is no data in the cache" - {

      "must set userAnswers to 'None' in the request" in {

        val sessionRepository = mock[SessionRepository]
        when(sessionRepository.get("id")) thenReturn Future(None)
        val action            = new Harness(sessionRepository)

        val futureResult = action.callRefine(IdentifierRequest(FakeRequest(), psaUser)).futureValue

        futureResult.left.map {
          result =>
            result.header.status mustBe SEE_OTHER
            result.header.headers.get("Location") mustBe controllers.routes.JourneyRecoveryController.onPageLoad()
        }
      }
    }

    "when there is data in the cache" - {

      "must build a userAnswers object and add it to the request" in {
        val userAnswers = UserAnswers("id")

        val sessionRepository = mock[SessionRepository]
        when(sessionRepository.get("id")) thenReturn Future(Some(userAnswers))
        val action            = new Harness(sessionRepository)

        val result = action.callRefine(IdentifierRequest(FakeRequest(), psaUser)).futureValue

        result.map {
          dataRequest =>
            dataRequest.userAnswers mustBe Some(userAnswers)
        }
      }
    }
  }
}

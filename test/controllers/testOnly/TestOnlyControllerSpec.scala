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

package controllers.testOnly

import base.SpecBase
import connectors.UserAnswersConnector
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Application
import play.api.http.Status.{BAD_GATEWAY, NO_CONTENT, OK}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, status}
import repositories.SessionRepository
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class TestOnlyControllerSpec extends AnyFreeSpec with Matchers with SpecBase {

  val mockUserAnswersConnector: UserAnswersConnector = mock[UserAnswersConnector]
  val mockSessionRepository: SessionRepository       = mock[SessionRepository]

  val application: Application = applicationBuilder(emptyUserAnswers)
    .overrides(
      bind[SessionRepository].toInstance(mockSessionRepository),
      bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
    ).build()

  val controller: TestOnlyController = application.injector.instanceOf[TestOnlyController]

  "resetDatabase" - {
    "return Ok when resetDatabase call returns No Content" in {
      when(mockSessionRepository.clear) thenReturn Future.successful(true)
      when(mockUserAnswersConnector.resetDatabase(any(), any())) thenReturn Future.successful(HttpResponse(NO_CONTENT))

      val request = FakeRequest("GET", "/test-only/reset-test-data")

      val result = controller.resetDatabase(request)

      status(result) mustBe OK
      contentAsString(result) mustBe "Success"
    }

    "return Bad Gateway when resetDatabase call returns any other status" in {
      when(mockSessionRepository.clear) thenReturn Future.successful(true)
      when(mockUserAnswersConnector.resetDatabase(any(), any())) thenReturn Future.successful(HttpResponse(BAD_GATEWAY))

      val request = FakeRequest("GET", "/test-only/reset-test-data")

      val result = controller.resetDatabase(request)

      status(result) mustBe BAD_GATEWAY
      contentAsString(result) mustBe "Reset failed. Try again"
    }
  }

}

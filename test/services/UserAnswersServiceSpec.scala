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

import base.SpecBase
import connectors.UserAnswersConnector
import models.UserAnswers
import models.dtos.UserAnswersDTO
import models.responses.{UserAnswersErrorResponse, UserAnswersSuccessResponse}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsObject, JsString}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserAnswersServiceSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  private val instant: Instant         = Instant.now
  private val id: String               = "id"
  private val mockUserAnswersConnector = mock[UserAnswersConnector]

  val service: UserAnswersService = new UserAnswersService(mockUserAnswersConnector)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "getUserAnswers" - {
    "return prepopulated UserAnswers when UserAnswersSuccessResponse is returned" in {
      val userAnswersDTO = UserAnswersDTO("id", JsObject(Map("field" -> JsString("value"))), instant)
      val userAnswers    = UserAnswers("id", JsObject(Map("field" -> JsString("value"))), instant)

      when(mockUserAnswersConnector.getAnswers(ArgumentMatchers.eq(id))(any(), any()))
        .thenReturn(Future.successful(UserAnswersSuccessResponse(userAnswersDTO)))

      val getUserAnswers = service.getUserAnswers(id)

      await(getUserAnswers) mustBe userAnswers
    }

    "return default UserAnswers with userId when UserAnswersFailureResponse is returned" in {
      when(mockUserAnswersConnector.getAnswers(ArgumentMatchers.eq(id))(any(), any()))
        .thenReturn(Future.successful(UserAnswersErrorResponse(new Throwable("throwable"))))

      val getUserAnswers = await(service.getUserAnswers(id))

      getUserAnswers.id mustBe id
      getUserAnswers.data mustBe JsObject.empty
    }
  }
}

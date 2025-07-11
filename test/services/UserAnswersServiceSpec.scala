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
import models.responses.{GetUserAnswersErrorResponse, GetUserAnswersNotFoundResponse, GetUserAnswersSuccessResponse, SetUserAnswersSuccessResponse}
import org.apache.pekko.Done
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
  private val mockUserAnswersConnector = mock[UserAnswersConnector]

  val service: UserAnswersService = new UserAnswersService(mockUserAnswersConnector)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val userAnswersDTO: UserAnswersDTO = UserAnswersDTO("id", JsObject(Map("field" -> JsString("value"))), instant)
  private val userAnswers: UserAnswers       = UserAnswers("id", JsObject(Map("field" -> JsString("value"))), instant)

  "getUserAnswers" - {
    "return prepopulated Right(UserAnswers) when GetUserAnswersSuccessResponse is returned" in {

      when(mockUserAnswersConnector.getAnswers(ArgumentMatchers.eq(userAnswersId))(any(), any()))
        .thenReturn(Future.successful(GetUserAnswersSuccessResponse(userAnswersDTO)))

      val getUserAnswers = service.getUserAnswers(userAnswersId)

      await(getUserAnswers) mustBe Right(userAnswers)
    }

    "return Right(UserAnswers) with default userId when GetUserAnswersNotFoundResponse is returned" in {
      when(mockUserAnswersConnector.getAnswers(ArgumentMatchers.eq(userAnswersId))(any(), any()))
        .thenReturn(Future.successful(GetUserAnswersNotFoundResponse))

      val getUserAnswers = await(service.getUserAnswers(userAnswersId))

      getUserAnswers map {
        ua =>
          ua.id mustBe userAnswersId
          ua.data mustBe JsObject.empty
      }
    }

    "return Left(GetUserAnswersErrorResponse) when GetUserErrorResponse is returned from connector" in {
      when(mockUserAnswersConnector.getAnswers(ArgumentMatchers.eq(userAnswersId))(any(), any()))
        .thenReturn(Future.successful(GetUserAnswersErrorResponse("Error message")))

      val getUserAnswers = await(service.getUserAnswers(userAnswersId))

      getUserAnswers.left.map {
        error => error.error mustBe "Error message"
      }
    }
  }

  "setUserAnswers" - {
    "return a Done status when Success is received from the connector" in {
      when(mockUserAnswersConnector.putAnswers(ArgumentMatchers.eq(userAnswersDTO))(any(), any()))
        .thenReturn(Future.successful(SetUserAnswersSuccessResponse))

      val setUserAnswers = await(service.setUserAnswers(userAnswers))

      setUserAnswers mustBe Done
    }
  }
}

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
import models.responses.{UserAnswersErrorResponse, UserAnswersNotFoundResponse}
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
    "return prepopulated Right(UserAnswers) when Left(GetUserAnswersSuccessResponse) is returned" in {

      when(mockUserAnswersConnector.getAnswers(ArgumentMatchers.eq(userAnswersId))(any(), any()))
        .thenReturn(Future.successful(Right(userAnswersDTO)))

      val getUserAnswers = service.getExternalUserAnswers(userAnswersId)

      await(getUserAnswers) mustBe Right(userAnswers)
    }

    "return Right(UserAnswers) with default userId when Left(GetUserAnswersNotFoundResponse) is returned" in {
      when(mockUserAnswersConnector.getAnswers(ArgumentMatchers.eq(userAnswersId))(any(), any()))
        .thenReturn(Future.successful(Left(UserAnswersNotFoundResponse)))

      val getUserAnswers = await(service.getExternalUserAnswers(userAnswersId))

      getUserAnswers map {
        ua =>
          ua.id mustBe userAnswersId
          ua.data mustBe JsObject.empty
      }
    }

    "return Left(GetUserAnswersErrorResponse) when Left(GetUserErrorResponse) is returned from connector" in {
      when(mockUserAnswersConnector.getAnswers(ArgumentMatchers.eq(userAnswersId))(any(), any()))
        .thenReturn(Future.successful(Left(UserAnswersErrorResponse("Error message", None))))

      val getUserAnswers = await(service.getExternalUserAnswers(userAnswersId))

      getUserAnswers mustBe Left(UserAnswersErrorResponse("Error message", None))
    }
  }

  "setUserAnswers" - {
    "return a Right(Done) status when Right(Done) is received from the connector" in {
      when(mockUserAnswersConnector.putAnswers(ArgumentMatchers.eq(userAnswersDTO))(any(), any()))
        .thenReturn(Future.successful(Right(Done)))

      val setUserAnswers = await(service.setExternalUserAnswers(userAnswers))

      setUserAnswers mustBe Right(Done)
    }

    "Return Left(error) when Left(error) is received from the connector" in {
      when(mockUserAnswersConnector.putAnswers(ArgumentMatchers.eq(userAnswersDTO))(any(), any()))
        .thenReturn(Future.successful(Left(UserAnswersErrorResponse("Error Message", None))))

      val setUserAnswers = await(service.setExternalUserAnswers(userAnswers))

      setUserAnswers mustBe Left(UserAnswersErrorResponse("Error Message", None))
    }
  }
}

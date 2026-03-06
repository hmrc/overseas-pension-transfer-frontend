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

package controllers.memberDetails

import base.SpecBase
import controllers.memberDetails.{routes => memberRoutes}
import controllers.routes
import models.NormalMode
import models.responses.UserAnswersErrorResponse
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.memberDetails.MembersLastUKAddressPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.UserAnswersService

import scala.concurrent.Future

class MembersLastUKAddressControllerOLDSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  private lazy val membersLastUKAddressRoute = memberRoutes.MembersLastUKAddressController.onPageLoad(NormalMode).url

  private val postCode = "AB1 2CD"

  "MembersLastUKAddress Controller (OLD)" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = userAnswersMemberNameQtNumber)
        .configure(
          "features.accessibility-address-changes" -> false
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, membersLastUKAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must redirect to the member date of leaving UK when valid data is submitted" in {
      val mockUserAnswersService = mock[UserAnswersService]
      val mockSessionRepository  = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockUserAnswersService.setExternalUserAnswers(any())(any()))
        .thenReturn(Future.successful(Right(Done)))

      val application = applicationBuilder(emptyUserAnswers)
        .configure(
          "features.accessibility-address-changes" -> false
        )
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UserAnswersService].toInstance(mockUserAnswersService)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, membersLastUKAddressRoute)
            .withFormUrlEncodedBody(("addressLine1", "1stLineAdd"), ("addressLine2", "2ndLineAdded"), ("postcode", postCode))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual MembersLastUKAddressPage.nextPage(NormalMode, emptyUserAnswers).url
      }
    }

    "must return a Bad Request when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = userAnswersMemberNameQtNumber)
        .configure(
          "features.accessibility-address-changes" -> false
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, membersLastUKAddressRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

    "must redirect to JourneyRecovery for a POST when userAnswersService returns a Left" in {
      val mockUserAnswersService = mock[UserAnswersService]
      val mockSessionRepository  = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockUserAnswersService.setExternalUserAnswers(any())(any()))
        .thenReturn(Future.successful(Left(UserAnswersErrorResponse("Error", None))))

      val application = applicationBuilder(userAnswersMemberNameQtNumber)
        .configure(
          "features.accessibility-address-changes" -> false
        )
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UserAnswersService].toInstance(mockUserAnswersService)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, membersLastUKAddressRoute)
            .withFormUrlEncodedBody(("addressLine1", "1stLineAdd"), ("addressLine2", "2ndLineAdded"), ("postcode", postCode))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

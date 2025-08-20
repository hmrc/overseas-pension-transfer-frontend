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

import base.AddressBase
import controllers.routes.JourneyRecoveryController
import forms.memberDetails.MemberConfirmLastUkAddressFormProvider
import models.{CheckMode, NormalMode}
import models.address.MembersLookupLastUkAddress
import models.responses.UserAnswersErrorResponse
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.memberDetails.MembersLastUkAddressConfirmPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.UserAnswersService
import viewmodels.AddressViewModel
import views.html.memberDetails.MembersLastUkAddressConfirmView

import scala.concurrent.Future

class MembersLastUkAddressConfirmControllerSpec extends AnyFreeSpec with MockitoSugar with AddressBase {

  private val formProvider                             = new MemberConfirmLastUkAddressFormProvider()
  private val form                                     = formProvider()
  private lazy val memberConfirmLastUkAddressGetRoute  = routes.MembersLastUkAddressConfirmController.onPageLoad(NormalMode).url
  private lazy val memberConfirmLastUkAddressPostRoute = routes.MembersLastUkAddressConfirmController.onSubmit(NormalMode, fromFinalCYA = false).url
  private val address                                  = MembersLookupLastUkAddress.fromAddressRecord(selectedRecord)

  "MemberConfirmLastUkAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(addressSelectedUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, memberConfirmLastUkAddressGetRoute)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[MembersLastUkAddressConfirmView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, address, false)(
          fakeDisplayRequest(request, addressSelectedUserAnswers),
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(addressSelectedUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, memberConfirmLastUkAddressGetRoute)
        val view    = application.injector.instanceOf[MembersLastUkAddressConfirmView]
        val result  = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, address, false)(
          fakeDisplayRequest(request, addressSelectedUserAnswers),
          messages(application)
        ).toString
      }
    }

    "must redirect to the date member left UK when continue is selected" in {
      val mockUserAnswersService = mock[UserAnswersService]
      val mockSessionRepository  = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockUserAnswersService.setExternalUserAnswers(any())(any()))
        .thenReturn(Future.successful(Right(Done)))

      val application = applicationBuilder(Some(addressSelectedUserAnswers))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UserAnswersService].toInstance(mockUserAnswersService)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, memberConfirmLastUkAddressPostRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual MembersLastUkAddressConfirmPage.nextPage(NormalMode, emptyUserAnswers).url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, memberConfirmLastUkAddressGetRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, memberConfirmLastUkAddressPostRoute)
            .withFormUrlEncodedBody(("value", "true"))
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery for a POST when userAnswersService returns a Left" in {
      val mockUserAnswersService = mock[UserAnswersService]
      val mockSessionRepository  = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockUserAnswersService.setExternalUserAnswers(any())(any()))
        .thenReturn(Future.successful(Left(UserAnswersErrorResponse("Error", None))))

      val application = applicationBuilder(Some(addressSelectedUserAnswers))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UserAnswersService].toInstance(mockUserAnswersService)
        )
        .build()

      running(application) {
        val req =
          FakeRequest(POST, memberConfirmLastUkAddressPostRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, req).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to final Check Your Answers page for a POST fromFinalCYA = true and Mode = CheckMode" in {
      val mockUserAnswersService = mock[UserAnswersService]
      val mockSessionRepository  = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockUserAnswersService.setExternalUserAnswers(any())(any()))
        .thenReturn(Future.successful(Right(Done)))

      val application = applicationBuilder(Some(addressSelectedUserAnswers))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UserAnswersService].toInstance(mockUserAnswersService)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, routes.MembersLastUkAddressConfirmController.onSubmit(CheckMode, fromFinalCYA = true).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.checkYourAnswers.routes.CheckYourAnswersController.onPageLoad().url
      }
    }
  }
}

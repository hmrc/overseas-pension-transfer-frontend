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
import controllers.routes.JourneyRecoveryController
import forms.memberDetails.MemberHasEverBeenResidentUKFormProvider
import models.address.MembersLastUKAddress
import models.responses.UserAnswersErrorResponse
import models.{CheckMode, NormalMode, UserAnswers}
import org.apache.pekko.Done
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.memberDetails.{MemberHasEverBeenResidentUKPage, MembersLastUKAddressPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.UserAnswersService
import views.html.memberDetails.MemberHasEverBeenResidentUKView

import scala.concurrent.Future

class MemberHasEverBeenResidentUKControllerSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  private val formProvider = new MemberHasEverBeenResidentUKFormProvider()
  private val form         = formProvider()

  private lazy val memberHasEverBeenResidentUKRoute = routes.MemberHasEverBeenResidentUKController.onPageLoad(NormalMode).url

  "memberHasEverBeenResidentUK Controller" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = userAnswersMemberNameQtNumber).build()

      running(application) {
        val req    = FakeRequest(GET, memberHasEverBeenResidentUKRoute)
        val result = route(application, req).value

        val view = application.injector.instanceOf[MemberHasEverBeenResidentUKView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(
          fakeDisplayRequest(req),
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = userAnswersMemberNameQtNumber.set(MemberHasEverBeenResidentUKPage, true).get
      val application = applicationBuilder(userAnswers = userAnswers).build()

      running(application) {
        val req    = FakeRequest(GET, memberHasEverBeenResidentUKRoute)
        val view   = application.injector.instanceOf[MemberHasEverBeenResidentUKView]
        val result = route(application, req).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(
          fakeDisplayRequest(req),
          messages(application)
        ).toString
      }
    }

    "must redirect to the Check Answers page when valid data is submitted in NormalMode" in {
      val mockUserAnswersService = mock[UserAnswersService]
      val mockSessionRepository  = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockUserAnswersService.setExternalUserAnswers(any())(any()))
        .thenReturn(Future.successful(Right(Done)))

      val application = applicationBuilder(emptyUserAnswers)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UserAnswersService].toInstance(mockUserAnswersService)
        )
        .build()

      running(application) {
        val req =
          FakeRequest(POST, memberHasEverBeenResidentUKRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, req).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.MemberDetailsCYAController.onPageLoad().url
      }
    }

    "must redirect to the Address Lookup page when valid data is submitted in NormalMode" in {
      val mockUserAnswersService = mock[UserAnswersService]
      val mockSessionRepository  = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockUserAnswersService.setExternalUserAnswers(any())(any()))
        .thenReturn(Future.successful(Right(Done)))

      val application = applicationBuilder(emptyUserAnswers)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UserAnswersService].toInstance(mockUserAnswersService)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, memberHasEverBeenResidentUKRoute)
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.MembersLastUkAddressLookupController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to next page in CheckMode if changed from false to true in CheckMode" in {
      val previousAnswers        = emptyUserAnswers.set(MemberHasEverBeenResidentUKPage, false).success.value
      val mockUserAnswersService = mock[UserAnswersService]
      val mockSessionRepository  = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockUserAnswersService.setExternalUserAnswers(any())(any()))
        .thenReturn(Future.successful(Right(Done)))

      val application = applicationBuilder(previousAnswers)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UserAnswersService].toInstance(mockUserAnswersService)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, routes.MemberHasEverBeenResidentUKController.onSubmit(CheckMode).url)
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.MembersLastUkAddressLookupController.onPageLoad(CheckMode).url
      }
    }

    "must remove MembersLastUKAddressPage if changed from true to false in CheckMode" in {
      val lastUkAdd       = MembersLastUKAddress("Line1", "Line2", Some("Line3"), Some("Line4"), "Postcode")
      val previousAnswers = emptyUserAnswers
        .set(MemberHasEverBeenResidentUKPage, true).success.value
        .set(MembersLastUKAddressPage, lastUkAdd).success.value

      val mockUserAnswersService = mock[UserAnswersService]
      val mockSessionRepository  = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockUserAnswersService.setExternalUserAnswers(any())(any()))
        .thenReturn(Future.successful(Right(Done)))

      val application = applicationBuilder(previousAnswers)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UserAnswersService].toInstance(mockUserAnswersService)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, routes.MemberHasEverBeenResidentUKController.onSubmit(CheckMode).url)
            .withFormUrlEncodedBody("value" -> "false")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.MemberDetailsCYAController.onPageLoad().url

        val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(captor.capture())

        val updatedAnswers = captor.getValue
        updatedAnswers.get(MembersLastUKAddressPage) mustBe None
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = userAnswersMemberNameQtNumber).build()

      running(application) {
        val req =
          FakeRequest(POST, memberHasEverBeenResidentUKRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))
        val view      = application.injector.instanceOf[MemberHasEverBeenResidentUKView]
        val result    = route(application, req).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(
          fakeDisplayRequest(req),
          messages(application)
        ).toString
      }
    }

    "must redirect to JourneyRecovery for a POST when userAnswersService returns a Left" in {
      val mockUserAnswersService = mock[UserAnswersService]
      val mockSessionRepository  = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockUserAnswersService.setExternalUserAnswers(any())(any()))
        .thenReturn(Future.successful(Left(UserAnswersErrorResponse("Error", None))))

      val application = applicationBuilder(emptyUserAnswers)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UserAnswersService].toInstance(mockUserAnswersService)
        )
        .build()

      running(application) {
        val req =
          FakeRequest(POST, memberHasEverBeenResidentUKRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, req).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

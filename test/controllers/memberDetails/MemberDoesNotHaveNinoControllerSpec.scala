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
import forms.memberDetails.MemberDoesNotHaveNinoFormProvider
import models.NormalMode
import models.responses.UserAnswersErrorResponse
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.memberDetails.MemberDoesNotHaveNinoPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.UserAnswersService
import views.html.memberDetails.MemberDoesNotHaveNinoView

import scala.concurrent.Future

class MemberDoesNotHaveNinoControllerSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  private val formProvider = new MemberDoesNotHaveNinoFormProvider()
  private val form         = formProvider()

  private lazy val memberDoesNotHaveNinoRoute = routes.MemberDoesNotHaveNinoController.onPageLoad(NormalMode).url

  "MemberDoesNotHaveNino Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersMemberNameQtNumber)).build()

      running(application) {
        val req    = FakeRequest(GET, memberDoesNotHaveNinoRoute)
        val result = route(application, req).value

        val view = application.injector.instanceOf[MemberDoesNotHaveNinoView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(
          fakeDisplayRequest(req),
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = userAnswersMemberNameQtNumber
        .set(MemberDoesNotHaveNinoPage, "answer").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val req    = FakeRequest(GET, memberDoesNotHaveNinoRoute)
        val view   = application.injector.instanceOf[MemberDoesNotHaveNinoView]
        val result = route(application, req).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), NormalMode)(
          fakeDisplayRequest(req, userAnswers),
          messages(application)
        ).toString
      }
    }

    "must redirect to the member date of birth when valid data is submitted" in {
      val mockUserAnswersService = mock[UserAnswersService]
      val mockSessionRepository  = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockUserAnswersService.setUserAnswers(any())(any()))
        .thenReturn(Future.successful(Right(Done)))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          )
          .build()

      running(application) {
        val req =
          FakeRequest(POST, memberDoesNotHaveNinoRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, req).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.MemberDateOfBirthController.onPageLoad(NormalMode).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersMemberNameQtNumber)).build()

      running(application) {
        val req =
          FakeRequest(POST, memberDoesNotHaveNinoRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))
        val view      = application.injector.instanceOf[MemberDoesNotHaveNinoView]
        val result    = route(application, req).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(
          fakeDisplayRequest(req),
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val req    = FakeRequest(GET, memberDoesNotHaveNinoRoute)
        val result = route(application, req).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val req =
          FakeRequest(POST, memberDoesNotHaveNinoRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, req).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery for a POST when userAnswersService returns a Left" in {
      val mockUserAnswersService = mock[UserAnswersService]
      val mockSessionRepository  = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockUserAnswersService.setUserAnswers(any())(any()))
        .thenReturn(Future.successful(Left(UserAnswersErrorResponse("Error", None))))

      val application = applicationBuilder(Some(userAnswersMemberNameQtNumber))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UserAnswersService].toInstance(mockUserAnswersService)
        )
        .build()

      running(application) {
        val req =
          FakeRequest(POST, memberDoesNotHaveNinoRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, req).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

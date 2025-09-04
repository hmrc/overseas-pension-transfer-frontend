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

package controllers

import base.SpecBase
import forms.DiscardTransferConfirmFormProvider
import models.NormalMode
import models.responses.UserAnswersErrorResponse
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.DiscardTransferConfirmPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.UserAnswersService
import views.html.DiscardTransferConfirmView

import scala.concurrent.Future

class DiscardTransferConfirmControllerSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  private val formProvider = new DiscardTransferConfirmFormProvider()
  private val form         = formProvider()

  private lazy val discardTransferConfirmRoute = routes.DiscardTransferConfirmController.onPageLoad().url

  "DiscardTransferConfirm Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = emptyUserAnswers).build()

      running(application) {
        val request = FakeRequest(GET, discardTransferConfirmRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DiscardTransferConfirmView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(DiscardTransferConfirmPage, true).success.value

      val application = applicationBuilder(userAnswers = userAnswers).build()

      running(application) {
        val request = FakeRequest(GET, discardTransferConfirmRoute)

        val view = application.injector.instanceOf[DiscardTransferConfirmView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true))(request, messages(application)).toString
      }
    }

    "must redirect and clear session and save for later when user answers true" in {
      val userAnswers = emptyUserAnswers.set(DiscardTransferConfirmPage, true).success.value

      val mockSessionRepository  = mock[SessionRepository]
      val mockUserAnswersService = mock[UserAnswersService]

      when(mockSessionRepository.clear(any())) thenReturn Future.successful(true)
      when(mockUserAnswersService.clearUserAnswers(any())(any())) thenReturn Future.successful(Right(Done))

      val application =
        applicationBuilder(userAnswers = userAnswers)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, discardTransferConfirmRoute)
            .withFormUrlEncodedBody(("discardTransfer", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.IndexController.onPageLoad().url

        verify(mockSessionRepository, times(1)).clear(any())
        verify(mockUserAnswersService, times(1)).clearUserAnswers(any())(any())
      }
    }

    "must redirect to the task list page when user answers false" in {
      val userAnswers = emptyUserAnswers.set(DiscardTransferConfirmPage, false).success.value

      val application =
        applicationBuilder(userAnswers = userAnswers).build()

      running(application) {
        val request =
          FakeRequest(POST, discardTransferConfirmRoute)
            .withFormUrlEncodedBody(("discardTransfer", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.TaskListController.onPageLoad().url

      }
    }

    "must return Internal Server Error when clearUserAnswers returns a Left(DeleteFailed)" in {
      val userAnswers = emptyUserAnswers.set(DiscardTransferConfirmPage, true).success.value

      val mockSessionRepository  = mock[SessionRepository]
      val mockUserAnswersService = mock[UserAnswersService]

      when(mockSessionRepository.clear(any())) thenReturn Future.successful(true)
      when(mockUserAnswersService.clearUserAnswers(any())(any())) thenReturn
        Future.successful(Left(UserAnswersErrorResponse("Error", None)))

      val application =
        applicationBuilder(userAnswers = userAnswers)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, discardTransferConfirmRoute)
            .withFormUrlEncodedBody(("discardTransfer", "true"))

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = emptyUserAnswers).build()

      running(application) {
        val request =
          FakeRequest(POST, discardTransferConfirmRoute)
            .withFormUrlEncodedBody(("discardTransfer", ""))

        val boundForm = form.bind(Map("discardTransfer" -> ""))

        val view = application.injector.instanceOf[DiscardTransferConfirmView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = emptyUserAnswers).build()

      running(application) {
        val request = FakeRequest(GET, discardTransferConfirmRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = emptyUserAnswers).build()

      running(application) {
        val request =
          FakeRequest(POST, discardTransferConfirmRoute)
            .withFormUrlEncodedBody(("discardTransfer", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

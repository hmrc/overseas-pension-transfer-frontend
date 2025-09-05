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

package controllers.qropsSchemeManagerDetails

import base.SpecBase
import controllers.routes.JourneyRecoveryController
import forms.qropsSchemeManagerDetails.SchemeManagerTypeFormProvider
import models.responses.UserAnswersErrorResponse
import models.{CheckMode, NormalMode, PersonName, SchemeManagerType, UserAnswers}
import org.apache.pekko.Done
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.qropsSchemeManagerDetails.{SchemeManagerTypePage, SchemeManagersNamePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.UserAnswersService
import views.html.qropsSchemeManagerDetails.SchemeManagerTypeView

import scala.concurrent.Future

class SchemeManagerTypeControllerSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  private lazy val schemeManagerTypeRoute = routes.SchemeManagerTypeController.onPageLoad(NormalMode).url

  private val formProvider = new SchemeManagerTypeFormProvider()
  private val form         = formProvider()

  "SchemeManagerType Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersQtNumber)).build()

      running(application) {
        val request = FakeRequest(GET, schemeManagerTypeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SchemeManagerTypeView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = userAnswersQtNumber.set(SchemeManagerTypePage, SchemeManagerType.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, schemeManagerTypeRoute)

        val view = application.injector.instanceOf[SchemeManagerTypeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(SchemeManagerType.values.head), NormalMode)(
          fakeDisplayRequest(request),
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val userAnswers = emptyUserAnswers.set(SchemeManagerTypePage, SchemeManagerType.values.head).success.value

      val mockUserAnswersService = mock[UserAnswersService]
      val mockSessionRepository  = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockUserAnswersService.setExternalUserAnswers(any())(any()))
        .thenReturn(Future.successful(Right(Done)))

      val application = applicationBuilder(Some(userAnswersMemberNameQtNumber))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UserAnswersService].toInstance(mockUserAnswersService)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, schemeManagerTypeRoute)
            .withFormUrlEncodedBody(("value", SchemeManagerType.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual SchemeManagerTypePage.nextPage(NormalMode, userAnswers).url
      }
    }

    "must redirect to next page in CheckMode if changed from 'Individual' to 'Organisation' in CheckMode" in {
      val previousAnswers        = emptyUserAnswers.set(SchemeManagerTypePage, SchemeManagerType.Individual).success.value
      val mockUserAnswersService = mock[UserAnswersService]
      val mockSessionRepository  = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockUserAnswersService.setExternalUserAnswers(any())(any()))
        .thenReturn(Future.successful(Right(Done)))

      val application = applicationBuilder(Some(previousAnswers))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UserAnswersService].toInstance(mockUserAnswersService)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, routes.SchemeManagerTypeController.onSubmit(CheckMode).url)
            .withFormUrlEncodedBody("value" -> SchemeManagerType.Organisation.toString)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SchemeManagerOrganisationNameController.onPageLoad(CheckMode).url
      }
    }

    "must remove previous data if SchemeManagerType changes" in {
      val mngrName        = PersonName("FirstNameMngr", "LastNameMngr")
      val previousAnswers = emptyUserAnswers
        .set(SchemeManagerTypePage, SchemeManagerType.Individual).success.value
        .set(SchemeManagersNamePage, mngrName).success.value

      val mockUserAnswersService = mock[UserAnswersService]
      val mockSessionRepository  = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockUserAnswersService.setExternalUserAnswers(any())(any()))
        .thenReturn(Future.successful(Right(Done)))

      val application = applicationBuilder(Some(previousAnswers))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UserAnswersService].toInstance(mockUserAnswersService)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, routes.SchemeManagerTypeController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody("value" -> SchemeManagerType.Organisation.toString)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SchemeManagerOrganisationNameController.onPageLoad(NormalMode).url

        val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(captor.capture())

        val updatedAnswers = captor.getValue
        updatedAnswers.get(SchemeManagersNamePage) mustBe None
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersQtNumber)).build()

      running(application) {
        val request =
          FakeRequest(POST, schemeManagerTypeRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[SchemeManagerTypeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, schemeManagerTypeRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, schemeManagerTypeRoute)
            .withFormUrlEncodedBody(("value", SchemeManagerType.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery for a POST when userAnswersService returns a Left" in {
      val mockUserAnswersService = mock[UserAnswersService]
      val mockSessionRepository  = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockUserAnswersService.setExternalUserAnswers(any())(any()))
        .thenReturn(Future.successful(Left(UserAnswersErrorResponse("Error", None))))

      val application = applicationBuilder(Some(userAnswersMemberNameQtNumber))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UserAnswersService].toInstance(mockUserAnswersService)
        )
        .build()

      running(application) {
        val req =
          FakeRequest(POST, schemeManagerTypeRoute)
            .withFormUrlEncodedBody(("value" -> SchemeManagerType.Organisation.toString))

        val result = route(application, req).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

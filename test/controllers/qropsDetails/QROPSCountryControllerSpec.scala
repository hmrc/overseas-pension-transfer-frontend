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

package controllers.qropsDetails

import base.AddressBase
import controllers.routes.JourneyRecoveryController
import forms.qropsDetails.QROPSCountryFormProvider
import models.NormalMode
import models.address.Country
import models.responses.UserAnswersErrorResponse
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.qropsDetails.QROPSCountryPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.{CountryService, UserAnswersService}
import viewmodels.CountrySelectViewModel
import views.html.qropsDetails.QROPSCountryView

import scala.concurrent.Future

class QROPSCountryControllerSpec extends AnyFreeSpec with AddressBase with MockitoSugar {

  private val formProvider = new QROPSCountryFormProvider()
  private val form         = formProvider()

  private val testCountries          = Seq(
    Country("GB", "United Kingdom"),
    Country("FR", "France")
  )
  private val countrySelectViewModel = CountrySelectViewModel.fromCountries(testCountries)
  private val mockCountryService     = mock[CountryService]

  private val userAnswers = emptyUserAnswers.set(QROPSCountryPage, testCountries.head).success.value

  private lazy val qropsCountryRoute = routes.QROPSCountryController.onPageLoad(NormalMode).url

  "QROPSCountry Controller" - {
    when(mockCountryService.countries).thenReturn(testCountries)

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[CountryService].toInstance(mockCountryService)
        ).build()

      running(application) {
        val request = FakeRequest(GET, qropsCountryRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[QROPSCountryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, countrySelectViewModel, NormalMode, false)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockCountryService.countries).thenReturn(testCountries)

      val application = applicationBuilder(Some(userAnswers))
        .overrides(
          bind[CountryService].toInstance(mockCountryService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, qropsCountryRoute)

        val view = application.injector.instanceOf[QROPSCountryView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(testCountries.head.code), countrySelectViewModel, NormalMode, false)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
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
          FakeRequest(POST, qropsCountryRoute)
            .withFormUrlEncodedBody(("countryCode", "GB"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual QROPSCountryPage.nextPage(NormalMode, emptyUserAnswers).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(
        bind[CountryService].toInstance(mockCountryService)
      ).build()

      running(application) {
        val request =
          FakeRequest(POST, qropsCountryRoute)
            .withFormUrlEncodedBody(("countryCode", ""))

        val boundForm = form.bind(Map("countryCode" -> ""))

        val view = application.injector.instanceOf[QROPSCountryView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, countrySelectViewModel, NormalMode, false)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, qropsCountryRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, qropsCountryRoute)
            .withFormUrlEncodedBody(("value", "answer"))

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
          FakeRequest(POST, qropsCountryRoute)
            .withFormUrlEncodedBody(("countryCode", "GB"))

        val result = route(application, req).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

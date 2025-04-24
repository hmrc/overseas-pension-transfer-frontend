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

import base.{AddressBase, SpecBase}
import forms.{QROPSAddressFormData, QROPSAddressFormProvider}
import models.NormalMode
import models.address.Country
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.QROPSAddressPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.CountryService
import viewmodels.CountrySelectViewModel
import views.html.QROPSAddressView

import scala.concurrent.Future

class QROPSAddressControllerSpec extends AnyFreeSpec with MockitoSugar with AddressBase {

  private val formProvider = new QROPSAddressFormProvider()
  private val form         = formProvider()
  private val formData     = QROPSAddressFormData.fromDomain(qropsAddress)

  private lazy val qropsAddressRoute = routes.QROPSAddressController.onPageLoad(NormalMode).url

  private val userAnswers = emptyUserAnswers.set(QROPSAddressPage, qropsAddress).success.value

  private val testCountries = Seq(
    Country("GB", "United Kingdom"),
    Country("FR", "France")
  )

  private val countrySelectViewModel = CountrySelectViewModel.fromCountries(testCountries)

  private val mockCountryService = mock[CountryService]

  "QROPSAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockCountryService.countries).thenReturn(testCountries)

      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(
          bind[CountryService].toInstance(mockCountryService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, qropsAddressRoute)
        val view    = application.injector.instanceOf[QROPSAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form,
          countrySelectViewModel,
          NormalMode
        )(request, messages(application)).toString
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
        val request = FakeRequest(GET, qropsAddressRoute)
        val view    = application.injector.instanceOf[QROPSAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(formData),
          countrySelectViewModel,
          NormalMode
        )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockCountryService.countries).thenReturn(testCountries)

      when(mockCountryService.find("GB"))
        .thenReturn(Some(Country("GB", "United Kingdom")))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CountryService].toInstance(mockCountryService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, qropsAddressRoute)
            .withFormUrlEncodedBody(
              "addressLine1" -> "value 1",
              "addressLine2" -> "value 2",
              "countryCode"  -> "GB"
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          QROPSAddressPage.nextPage(NormalMode, emptyUserAnswers).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockCountryService.countries).thenReturn(testCountries)

      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(
          bind[CountryService].toInstance(mockCountryService)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, qropsAddressRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))
        val view      = application.injector.instanceOf[QROPSAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(
          boundForm,
          countrySelectViewModel,
          NormalMode
        )(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, qropsAddressRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, qropsAddressRoute)
            .withFormUrlEncodedBody(
              "addressLine1" -> "value 1",
              "addressLine2" -> "value 2"
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

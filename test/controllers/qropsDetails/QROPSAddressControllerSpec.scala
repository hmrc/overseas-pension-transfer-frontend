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
import config.FrontendAppConfig
import controllers.qropsDetails.{routes => qropRoutes}
import controllers.{routes => baseRoutes}
import forms.qropsDetails.{QROPSAddressFormData, QROPSAddressFormProvider}
import models.NormalMode
import models.address.Country
import models.responses.UserAnswersErrorResponse
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.qropsDetails.QROPSAddressPage
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.{AddressService, CountryService, UserAnswersService}
import viewmodels.CountrySelectViewModel
import views.html.qropsDetails.QROPSAddressView

import scala.concurrent.Future

class QROPSAddressControllerSpec extends AnyFreeSpec with MockitoSugar with AddressBase {

  private val formProvider = new QROPSAddressFormProvider()
  private val form         = formProvider()
  private val formData     = QROPSAddressFormData.fromDomain(qropsAddress)

  private lazy val qropsAddressRoute = qropRoutes.QROPSAddressController.onPageLoad(NormalMode).url

  private val userAnswers = emptyUserAnswers.set(QROPSAddressPage, qropsAddress).success.value

  private val testCountries = Seq(
    Country("GB", "United Kingdom"),
    Country("FR", "France")
  )

  implicit private val messages: Messages = stubMessages()
  private val countrySelectViewModel      = CountrySelectViewModel.fromCountries(testCountries)

  private val mockCountryService = mock[CountryService]

  "QROPSAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockCountryService.countries).thenReturn(testCountries)

      val application = applicationBuilder(emptyUserAnswers)
        .overrides(
          bind[CountryService].toInstance(mockCountryService)
        )
        .configure(
          "features.accessibility-address-changes" -> false
        )
        .build()

      running(application) {
        val request   = FakeRequest(GET, qropsAddressRoute)
        val view      = application.injector.instanceOf[QROPSAddressView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form,
          countrySelectViewModel,
          NormalMode
        )(request, messages(application), appConfig).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockCountryService.countries).thenReturn(testCountries)

      val application = applicationBuilder(userAnswers)
        .overrides(
          bind[CountryService].toInstance(mockCountryService)
        )
        .configure(
          "features.accessibility-address-changes" -> false
        )
        .build()

      running(application) {
        val request   = FakeRequest(GET, qropsAddressRoute)
        val view      = application.injector.instanceOf[QROPSAddressView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(formData),
          countrySelectViewModel,
          NormalMode
        )(request, messages(application), appConfig).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val mockUserAnswersService = mock[UserAnswersService]
      val mockSessionRepository  = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockUserAnswersService.setExternalUserAnswers(any())(any()))
        .thenReturn(Future.successful(Right(Done)))

      when(mockCountryService.countries).thenReturn(testCountries)

      when(mockCountryService.find("GB"))
        .thenReturn(Some(Country("GB", "United Kingdom")))

      val application = applicationBuilder(emptyUserAnswers)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UserAnswersService].toInstance(mockUserAnswersService),
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

      val application = applicationBuilder(emptyUserAnswers)
        .overrides(
          bind[CountryService].toInstance(mockCountryService)
        )
        .configure(
          "features.accessibility-address-changes" -> false
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, qropsAddressRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))
        val view      = application.injector.instanceOf[QROPSAddressView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(
          boundForm,
          countrySelectViewModel,
          NormalMode
        )(request, messages(application), appConfig).toString
      }
    }

    "must return a Bad Request and errors when the postcode is provided but the country is not the UK" in {

      val mockAddressService = mock[AddressService]

      when(mockCountryService.countries).thenReturn(testCountries)
      when(mockAddressService.qropsAddress(any())).thenReturn(
        Some(qropsAddress.copy(addressLine4 = Some("AA00AA"), country = Country("FR", "France")))
      )

      val application = applicationBuilder(emptyUserAnswers)
        .overrides(
          bind[CountryService].toInstance(mockCountryService),
          bind[AddressService].toInstance(mockAddressService)
        )
        .configure(
          "features.accessibility-address-changes" -> false
        )
        .build()

      val data = Seq(
        ("addressLine1", "Some Street"),
        ("addressLine2", "Some Area"),
        ("addressLine4", "AA00AA"),
        ("countryCode", "FR")
      )

      running(application) {
        val request =
          FakeRequest(POST, qropsAddressRoute)
            .withFormUrlEncodedBody(data: _*)

        val boundForm = form.bind(Map(data: _*))
        val view      = application.injector.instanceOf[QROPSAddressView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(
          boundForm.withError("addressLine4", "membersLastUKAddress.error.postcode.incorrect"),
          countrySelectViewModel,
          NormalMode
        )(request, messages(application), appConfig).toString
      }
    }

    "must redirect to JourneyRecovery for a POST when userAnswersService returns a Left" in {
      val mockUserAnswersService = mock[UserAnswersService]
      val mockSessionRepository  = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockUserAnswersService.setExternalUserAnswers(any())(any()))
        .thenReturn(Future.successful(Left(UserAnswersErrorResponse("Error", None))))

      when(mockCountryService.countries).thenReturn(testCountries)

      when(mockCountryService.find("GB"))
        .thenReturn(Some(Country("GB", "United Kingdom")))

      val application = applicationBuilder(userAnswersMemberNameQtNumber)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UserAnswersService].toInstance(mockUserAnswersService),
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
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

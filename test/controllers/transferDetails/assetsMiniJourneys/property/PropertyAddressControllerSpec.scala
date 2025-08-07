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

package controllers.transferDetails.assetsMiniJourneys.property

import base.AddressBase
import controllers.routes.JourneyRecoveryController
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import forms.transferDetails.assetsMiniJourneys.property.{PropertyAddressFormData, PropertyAddressFormProvider}
import models.NormalMode
import models.address._
import models.requests.DisplayRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.transferDetails.assetsMiniJourneys.property.PropertyAddressPage
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.CountryService
import viewmodels.CountrySelectViewModel
import views.html.transferDetails.assetsMiniJourneys.property.PropertyAddressView

import scala.concurrent.Future

class PropertyAddressControllerSpec extends AnyFreeSpec with MockitoSugar with AddressBase {
  private val index = 0

  private val formProvider = new PropertyAddressFormProvider()
  private val formData     = PropertyAddressFormData.fromDomain(propertyAddress)

  private lazy val propertyAddressRoute = AssetsMiniJourneysRoutes.PropertyAddressController.onPageLoad(NormalMode, index).url

  private val testCountries = Seq(
    Country("GB", "United Kingdom"),
    Country("FR", "France")
  )

  private val countrySelectViewModel = CountrySelectViewModel.fromCountries(testCountries)

  private val mockCountryService = mock[CountryService]

  "PropertyAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersMemberNameQtNumber)).overrides(
        bind[CountryService].toInstance(mockCountryService)
      ).build()

      when(mockCountryService.countries).thenReturn(testCountries)

      running(application) {
        val request                                                         = FakeRequest(GET, propertyAddressRoute)
        implicit val displayRequest: DisplayRequest[AnyContentAsEmpty.type] = fakeDisplayRequest(request)

        val form = formProvider()
        val view = application.injector.instanceOf[PropertyAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(
          form,
          countrySelectViewModel,
          NormalMode,
          index
        )(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers =
        userAnswersMemberNameQtNumber.set(PropertyAddressPage(index), propertyAddress).success.value
      val application = applicationBuilder(Some(userAnswers))
        .overrides(
          bind[CountryService].toInstance(mockCountryService)
        )
        .build()

      when(mockCountryService.countries).thenReturn(testCountries)

      running(application) {
        val request                                                         = FakeRequest(GET, propertyAddressRoute)
        implicit val displayRequest: DisplayRequest[AnyContentAsEmpty.type] = fakeDisplayRequest(request)

        val form   = formProvider()
        val view   = application.injector.instanceOf[PropertyAddressView]
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(formData),
          countrySelectViewModel,
          NormalMode,
          index
        )(displayRequest, messages(application)).toString
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
          FakeRequest(POST, propertyAddressRoute)
            .withFormUrlEncodedBody(
              "addressLine1" -> "value 1",
              "addressLine2" -> "value 2",
              "countryCode"  -> "GB"
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          PropertyAddressPage(index).nextPage(NormalMode, emptyUserAnswers).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(Some(userAnswersMemberNameQtNumber))
        .overrides(
          bind[CountryService].toInstance(mockCountryService)
        )
        .build()

      when(mockCountryService.countries).thenReturn(testCountries)

      running(application) {
        val request =
          FakeRequest(POST, propertyAddressRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        implicit val displayRequest: DisplayRequest[AnyContentAsFormUrlEncoded] = fakeDisplayRequest(request)

        val form      = formProvider()
        val boundForm = form.bind(Map("value" -> "invalid value"))
        val view      = application.injector.instanceOf[PropertyAddressView]
        val result    = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, countrySelectViewModel, NormalMode, index)(
          displayRequest,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, propertyAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, propertyAddressRoute)
            .withFormUrlEncodedBody(("addressLine1", "value 1"), ("addressLine2", "value 2"))
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

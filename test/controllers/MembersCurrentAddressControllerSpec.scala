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
import forms.MembersCurrentAddressFormProvider
import models.NormalMode
import base.{AddressBase, SpecBase}
import forms.{MembersCurrentAddressFormData, MembersCurrentAddressFormProvider}
import models.{NormalMode, PersonName}
import models.address._
import models.requests.DisplayRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.MembersCurrentAddressPage
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.CountryService
import views.html.MembersCurrentAddressView
import viewmodels.CountrySelectViewModel

import scala.concurrent.Future

class MembersCurrentAddressControllerSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  private val formProvider = new MembersCurrentAddressFormProvider()

  private lazy val membersCurrentAddressRoute = routes.MembersCurrentAddressController.onPageLoad(NormalMode).url

  private val validAnswer = MembersCurrentAddress("value 1", "value 2", Some("value 3"), Some("value 4"), Some("value 5"), Some("value 6"))
  private val testCountries = Seq(
    Country("GB", "United Kingdom"),
    Country("FR", "France")
  )

  private val countrySelectViewModel = CountrySelectViewModel.fromCountries(testCountries)

  private val mockCountryService = mock[CountryService]

  "MembersCurrentAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersMemberNameQtNumber)).build()
      when(mockCountryService.countries).thenReturn(testCountries)

      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(
          bind[CountryService].toInstance(mockCountryService)
        )
        .build()

      running(application) {
        val request                                                         = FakeRequest(GET, membersCurrentAddressRoute)
        implicit val displayRequest: DisplayRequest[AnyContentAsEmpty.type] = fakeDisplayRequest(request)

        val form   = formProvider()
        val view   = application.injector.instanceOf[MembersCurrentAddressView]
        val request = FakeRequest(GET, membersCurrentAddressRoute)
        val view    = application.injector.instanceOf[MembersCurrentAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(
          form,
          countrySelectViewModel,
          NormalMode
        )(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = userAnswersMemberNameQtNumber.set(MembersCurrentAddressPage, validAnswer).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      when(mockCountryService.countries).thenReturn(testCountries)

      val application = applicationBuilder(Some(userAnswers))
        .overrides(
          bind[CountryService].toInstance(mockCountryService)
        )
        .build()

      running(application) {
        val request                                                         = FakeRequest(GET, membersCurrentAddressRoute)
        implicit val displayRequest: DisplayRequest[AnyContentAsEmpty.type] = fakeDisplayRequest(request)

        val form   = formProvider()
        val view   = application.injector.instanceOf[MembersCurrentAddressView]
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(formData),
          countrySelectViewModel,
          NormalMode
        )(displayRequest, messages(application)).toString
      }
    }

    "must redirect to the member is UK resident when valid data is submitted" in {

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
          FakeRequest(POST, membersCurrentAddressRoute)
            .withFormUrlEncodedBody(
              "addressLine1" -> "value 1",
              "addressLine2" -> "value 2",
              "countryCode"  -> "GB"
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          MembersCurrentAddressPage.nextPage(NormalMode, emptyUserAnswers).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersMemberNameQtNumber)).build()
      when(mockCountryService.countries).thenReturn(testCountries)

      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(
          bind[CountryService].toInstance(mockCountryService)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, membersCurrentAddressRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        implicit val displayRequest: DisplayRequest[AnyContentAsFormUrlEncoded] = fakeDisplayRequest(request)

        val form      = formProvider()
        val boundForm = form.bind(Map("value" -> "invalid value"))
        val view      = application.injector.instanceOf[MembersCurrentAddressView]
        val result    = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(
          displayRequest,
          countrySelectViewModel,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, membersCurrentAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, membersCurrentAddressRoute)
            .withFormUrlEncodedBody(("addressLine1", "value 1"), ("addressLine2", "value 2"))
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

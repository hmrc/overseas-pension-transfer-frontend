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
import forms.memberDetails.{MembersCurrentAddressFormData, MembersCurrentAddressFormProvider}
import models.{CheckMode, NormalMode}
import models.address._
import models.requests.DisplayRequest
import models.responses.UserAnswersErrorResponse
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.memberDetails.{MemberNinoPage, MembersCurrentAddressPage}
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.{CountryService, UserAnswersService}
import viewmodels.CountrySelectViewModel
import views.html.memberDetails.MembersCurrentAddressView

import scala.concurrent.Future

class MembersCurrentAddressControllerSpec extends AnyFreeSpec with MockitoSugar with AddressBase {

  private val formProvider = new MembersCurrentAddressFormProvider()
  private val formData     = MembersCurrentAddressFormData.fromDomain(membersCurrentAddress)

  private lazy val membersCurrentAddressGetRoute  = routes.MembersCurrentAddressController.onPageLoad(NormalMode).url
  private lazy val membersCurrentAddressPostRoute = routes.MembersCurrentAddressController.onSubmit(NormalMode, fromFinalCYA = false).url

  private val testCountries = Seq(
    Country("GB", "United Kingdom"),
    Country("FR", "France")
  )

  private val countrySelectViewModel = CountrySelectViewModel.fromCountries(testCountries)

  private val mockCountryService = mock[CountryService]

  "MembersCurrentAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersMemberNameQtNumber)).overrides(
        bind[CountryService].toInstance(mockCountryService)
      ).build()

      when(mockCountryService.countries).thenReturn(testCountries)

      running(application) {
        val request                                                         = FakeRequest(GET, membersCurrentAddressGetRoute)
        implicit val displayRequest: DisplayRequest[AnyContentAsEmpty.type] = fakeDisplayRequest(request)

        val form = formProvider()
        val view = application.injector.instanceOf[MembersCurrentAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(
          form,
          countrySelectViewModel,
          NormalMode,
          false
        )(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers =
        userAnswersMemberNameQtNumber.set(MembersCurrentAddressPage, membersCurrentAddress).success.value
      val application = applicationBuilder(Some(userAnswers))
        .overrides(
          bind[CountryService].toInstance(mockCountryService)
        )
        .build()

      when(mockCountryService.countries).thenReturn(testCountries)

      running(application) {
        val request                                                         = FakeRequest(GET, membersCurrentAddressGetRoute)
        implicit val displayRequest: DisplayRequest[AnyContentAsEmpty.type] = fakeDisplayRequest(request)

        val form   = formProvider()
        val view   = application.injector.instanceOf[MembersCurrentAddressView]
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(formData),
          countrySelectViewModel,
          NormalMode,
          false
        )(displayRequest, messages(application)).toString
      }
    }

    "must redirect to the member is UK resident when valid data is submitted" in {
      val mockUserAnswersService = mock[UserAnswersService]
      val mockSessionRepository  = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockCountryService.countries).thenReturn(testCountries)

      when(mockCountryService.find("GB"))
        .thenReturn(Some(Country("GB", "United Kingdom")))

      when(mockUserAnswersService.setExternalUserAnswers(any())(any()))
        .thenReturn(Future.successful(Right(Done)))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CountryService].toInstance(mockCountryService),
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, membersCurrentAddressPostRoute)
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

      val application = applicationBuilder(Some(userAnswersMemberNameQtNumber))
        .overrides(
          bind[CountryService].toInstance(mockCountryService)
        )
        .build()

      when(mockCountryService.countries).thenReturn(testCountries)

      running(application) {
        val request =
          FakeRequest(POST, membersCurrentAddressPostRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        implicit val displayRequest: DisplayRequest[AnyContentAsFormUrlEncoded] = fakeDisplayRequest(request)

        val form      = formProvider()
        val boundForm = form.bind(Map("value" -> "invalid value"))
        val view      = application.injector.instanceOf[MembersCurrentAddressView]
        val result    = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, countrySelectViewModel, NormalMode, false)(
          displayRequest,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, membersCurrentAddressGetRoute)

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
          FakeRequest(POST, membersCurrentAddressPostRoute)
            .withFormUrlEncodedBody(("addressLine1", "value 1"), ("addressLine2", "value 2"))
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

      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UserAnswersService].toInstance(mockUserAnswersService)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, membersCurrentAddressPostRoute)
            .withFormUrlEncodedBody(
              "addressLine1" -> "value 1",
              "addressLine2" -> "value 2",
              "countryCode"  -> "GB"
            )

        val result = route(application, request).value

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

      val application = applicationBuilder(Some(userAnswersMemberNameQtNumber))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UserAnswersService].toInstance(mockUserAnswersService)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, routes.MembersCurrentAddressController.onSubmit(CheckMode, fromFinalCYA = true).url)
            .withFormUrlEncodedBody(
              "addressLine1" -> "value 1",
              "addressLine2" -> "value 2",
              "countryCode"  -> "GB"
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.checkYourAnswers.routes.CheckYourAnswersController.onPageLoad().url
      }
    }
  }
}

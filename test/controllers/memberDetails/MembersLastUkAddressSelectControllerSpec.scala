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
import forms.memberDetails.MembersLastUkAddressSelectFormProvider
import models.NormalMode
import models.address.MembersLookupLastUkAddress
import models.responses.UserAnswersErrorResponse
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.memberDetails.MembersLastUkAddressSelectPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.UserAnswersService
import viewmodels.AddressViewModel
import views.html.memberDetails.MembersLastUkAddressSelectView

import scala.concurrent.Future

class MembersLastUkAddressSelectControllerSpec extends AnyFreeSpec with MockitoSugar with AddressBase {

  private lazy val memberSelectLastUkAddressRoute = routes.MembersLastUkAddressSelectController.onPageLoad(NormalMode).url

  private val formProvider = new MembersLastUkAddressSelectFormProvider()
  private val form         = formProvider(validIds)
  private val idAddressMap = addressRecordList.map(r => r.id -> MembersLookupLastUkAddress.fromAddressRecord(r)).toMap

  "MemberSelectLastUkAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(addressFoundUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, memberSelectLastUkAddressRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MembersLastUkAddressSelectView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form,
          NormalMode,
          AddressViewModel.addressRadios(idsWithAddresses = idAddressMap.toSeq),
          addressRecords.postcode
        )(
          fakeDisplayRequest(request),
          messages(application)
        ).toString
      }
    }

    "must redirect to the address confirmation when valid data is submitted" in {

      val mockUserAnswersService = mock[UserAnswersService]
      val mockSessionRepository  = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockUserAnswersService.setUserAnswers(any())(any()))
        .thenReturn(Future.successful(Right(Done)))

      val application = applicationBuilder(Some(addressFoundUserAnswers))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UserAnswersService].toInstance(mockUserAnswersService)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, memberSelectLastUkAddressRoute)
            .withFormUrlEncodedBody(("value", validIds.head))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual MembersLastUkAddressSelectPage.nextPage(NormalMode, emptyUserAnswers).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(addressFoundUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, memberSelectLastUkAddressRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))
        val view      = application.injector.instanceOf[MembersLastUkAddressSelectView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(
          boundForm,
          NormalMode,
          AddressViewModel.addressRadios(idAddressMap.toSeq),
          addressRecords.postcode
        )(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, memberSelectLastUkAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, memberSelectLastUkAddressRoute)
            .withFormUrlEncodedBody(("value", validIds.head))

        val result = route(application, request).value

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

      val application = applicationBuilder(Some(addressFoundUserAnswers))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UserAnswersService].toInstance(mockUserAnswersService)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, memberSelectLastUkAddressRoute)
            .withFormUrlEncodedBody(("value", validIds.head))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

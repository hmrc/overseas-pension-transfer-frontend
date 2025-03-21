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
import connectors.{AddressLookupConnector, AddressLookupErrorResponse, AddressLookupSuccessResponse}
import forms.MembersLastUkAddressLookupFormProvider
import models.NormalMode
import models.address.RecordSet
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.MembersLastUkAddressLookupPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.MembersLastUkAddressLookupView

import scala.concurrent.Future

class MembersLastUkAddressLookupControllerSpec extends SpecBase with MockitoSugar with AddressBase {

  private val formProvider = new MembersLastUkAddressLookupFormProvider()
  private val form         = formProvider()

  private lazy val membersLastUkAddressLookupRoute = routes.MembersLastUkAddressLookupController.onPageLoad(NormalMode).url

  "MembersLastUkAddressLookup Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, membersLastUkAddressLookupRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MembersLastUkAddressLookupView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository      = mock[SessionRepository]
      val mockAddressLookupConnector = mock[AddressLookupConnector]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockAddressLookupConnector.lookup(any())(any(), any()))
        .thenReturn(
          Future.successful(
            AddressLookupSuccessResponse(connectorPostcode, recordSet)
          )
        )

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[AddressLookupConnector].toInstance(mockAddressLookupConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, membersLastUkAddressLookupRoute)
            .withFormUrlEncodedBody(("value", connectorPostcode))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual MembersLastUkAddressLookupPage.nextPage(NormalMode, emptyUserAnswers).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, membersLastUkAddressLookupRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[MembersLastUkAddressLookupView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to nextPageNoResults when the connector returns a success but no addresses found" in {
      val mockSessionRepository      = mock[SessionRepository]
      val mockAddressLookupConnector = mock[AddressLookupConnector]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val noAddresses = RecordSet(Seq.empty)
      when(mockAddressLookupConnector.lookup(any())(any(), any()))
        .thenReturn(
          Future.successful(
            AddressLookupSuccessResponse("AB1 2CD", noAddresses)
          )
        )

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[AddressLookupConnector].toInstance(mockAddressLookupConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, membersLastUkAddressLookupRoute)
            .withFormUrlEncodedBody(("value", "ZZ1 1ZZ"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual MembersLastUkAddressLookupPage.nextPageNoResults().url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, membersLastUkAddressLookupRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to nextPageRecovery when the connector returns an error" in {
      val mockSessionRepository      = mock[SessionRepository]
      val mockAddressLookupConnector = mock[AddressLookupConnector]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockAddressLookupConnector.lookup(any())(any(), any()))
        .thenReturn(
          Future.successful(
            AddressLookupErrorResponse(new RuntimeException("Simulated address service failure"))
          )
        )

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[AddressLookupConnector].toInstance(mockAddressLookupConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, membersLastUkAddressLookupRoute)
            .withFormUrlEncodedBody(("value", "AB1 2CD"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          MembersLastUkAddressLookupPage.nextPageRecovery(
            Some(MembersLastUkAddressLookupPage.recoveryModeReturnUrl)
          ).url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, membersLastUkAddressLookupRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

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
import forms.MemberConfirmLastUkAddressFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.MemberConfirmLastUkAddressPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import viewmodels.AddressViewModel
import views.html.MemberConfirmLastUkAddressView

import scala.concurrent.Future

class MemberConfirmLastUkAddressControllerSpec extends SpecBase with MockitoSugar with AddressBase {

  private val formProvider = new MemberConfirmLastUkAddressFormProvider()
  private val form         = formProvider()

  private lazy val memberConfirmLastUkAddressRoute = routes.MemberConfirmLastUkAddressController.onPageLoad(NormalMode).url

  "MemberConfirmLastUkAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(addressSelectedUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, memberConfirmLastUkAddressRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MemberConfirmLastUkAddressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, AddressViewModel.fromAddress(selectedAddress.address))(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(addressSelectedUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, memberConfirmLastUkAddressRoute)

        val view = application.injector.instanceOf[MemberConfirmLastUkAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, AddressViewModel.fromAddress(selectedAddress.address))(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when continue is selected" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(addressSelectedUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, memberConfirmLastUkAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual MemberConfirmLastUkAddressPage.nextPage(NormalMode, emptyUserAnswers).url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, memberConfirmLastUkAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, memberConfirmLastUkAddressRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must clear lookup addresses on POST" in {}
  }
}

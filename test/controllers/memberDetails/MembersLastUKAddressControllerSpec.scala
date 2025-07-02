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
import forms.memberDetails.MembersLastUKAddressFormProvider
import models.NormalMode
import models.requests.DisplayRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.memberDetails.MembersLastUKAddressPage
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.memberDetails.MembersLastUKAddressView

import scala.concurrent.Future

class MembersLastUKAddressControllerSpec extends AnyFreeSpec with AddressBase with MockitoSugar {
  private val formProvider = new MembersLastUKAddressFormProvider()

  private lazy val membersLastUKAddressRoute = routes.MembersLastUKAddressController.onPageLoad(NormalMode).url

  private val postCode            = "AB1 2CD"
  private val validAnswer         = membersLastUKAddress
  private val validAnswerFormData = membersLastUKAddressFormData

  "MembersLastUKAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersMemberNameQtNumber)).build()

      running(application) {
        val request                                                         = FakeRequest(GET, membersLastUKAddressRoute)
        implicit val displayRequest: DisplayRequest[AnyContentAsEmpty.type] = fakeDisplayRequest(request)

        val form = formProvider()
        val view = application.injector.instanceOf[MembersLastUKAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(displayRequest, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = userAnswersMemberNameQtNumber.set(MembersLastUKAddressPage, validAnswer).get

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request                                                         = FakeRequest(GET, membersLastUKAddressRoute)
        implicit val displayRequest: DisplayRequest[AnyContentAsEmpty.type] = fakeDisplayRequest(request)

        val form = formProvider()
        val view = application.injector.instanceOf[MembersLastUKAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(validAnswerFormData),
          NormalMode
        )(displayRequest, messages(application)).toString
      }
    }

    "must redirect to the member date of leaving UK when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, membersLastUKAddressRoute)
            .withFormUrlEncodedBody(("addressLine1", "1stLineAdd"), ("addressLine2", "2ndLineAdded"), ("postcode", postCode))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual MembersLastUKAddressPage.nextPage(NormalMode, emptyUserAnswers).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersMemberNameQtNumber)).build()

      running(application) {
        val request                                                             =
          FakeRequest(POST, membersLastUKAddressRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))
        implicit val displayRequest: DisplayRequest[AnyContentAsFormUrlEncoded] = fakeDisplayRequest(request)

        val form      = formProvider()
        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[MembersLastUKAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(displayRequest, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, membersLastUKAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, membersLastUKAddressRoute)
            .withFormUrlEncodedBody(("addressLine1", "1stLineAdd"), ("addressLine2", "2ndLineAdd"), ("postcode", postCode))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

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
import forms.MemberIsResidentUKFormProvider
import models.address.MembersLastUKAddress
import models.{CheckMode, NormalMode, PersonName, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{MemberHasEverBeenResidentUKPage, MemberIsResidentUKPage, MembersLastUKAddressPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.MemberIsResidentUKView

import scala.concurrent.Future

class MemberIsResidentUKControllerSpec extends SpecBase with MockitoSugar {

  private val memberName   = PersonName("Undefined", "Undefined")
  private val formProvider = new MemberIsResidentUKFormProvider()
  private val form         = formProvider()

  private lazy val memberIsResidentUKRoute = routes.MemberIsResidentUKController.onPageLoad(NormalMode).url

  "MemberIsResidentUK Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, memberIsResidentUKRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MemberIsResidentUKView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, memberName.fullName, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(MemberIsResidentUKPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, memberIsResidentUKRoute)

        val view = application.injector.instanceOf[MemberIsResidentUKView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), memberName.fullName, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

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
          FakeRequest(POST, memberIsResidentUKRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.MemberHasEverBeenResidentUKController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to NormalMode if changed from true to false in CheckMode" in {
      val previousAnswers = emptyUserAnswers.set(MemberIsResidentUKPage, true).success.value
      val application     = applicationBuilder(userAnswers = Some(previousAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, routes.MemberIsResidentUKController.onSubmit(CheckMode).url)
            .withFormUrlEncodedBody("value" -> "false")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.MemberHasEverBeenResidentUKController.onPageLoad(NormalMode).url
      }
    }

    "must remove previous data if changed from false to true in CheckMode" in {
      val lastUkAdd       = MembersLastUKAddress("Line1", "Line2", Some("Line3"), Some("Line4"), "Postcode")
      val previousAnswers = emptyUserAnswers
        .set(MemberIsResidentUKPage, false).success.value
        .set(MemberHasEverBeenResidentUKPage, true).success.value
        .set(MembersLastUKAddressPage, lastUkAdd).success.value

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(previousAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, routes.MemberIsResidentUKController.onSubmit(CheckMode).url)
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.MemberDetailsCYAController.onPageLoad().url

        val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(captor.capture())

        val updatedAnswers = captor.getValue
        updatedAnswers.get(MemberHasEverBeenResidentUKPage) mustBe None
        updatedAnswers.get(MembersLastUKAddressPage) mustBe None
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, memberIsResidentUKRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[MemberIsResidentUKView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, memberName.fullName, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, memberIsResidentUKRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, memberIsResidentUKRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

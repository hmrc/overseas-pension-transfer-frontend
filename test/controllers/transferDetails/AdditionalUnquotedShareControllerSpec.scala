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

package controllers.transferDetails

import base.SpecBase
import forms.transferDetails.AdditionalUnquotedShareFormProvider
import models.NormalMode
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.transferDetails.AdditionalUnquotedShareView

class AdditionalUnquotedShareControllerSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  private val formProvider = new AdditionalUnquotedShareFormProvider()
  private val form         = formProvider()

  private lazy val additionalUnquotedShareRoute = routes.AdditionalUnquotedShareController.onPageLoad().url

  "AdditionalUnquotedShare Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersQtNumber)).build()

      running(application) {
        val request = FakeRequest(GET, additionalUnquotedShareRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AdditionalUnquotedShareView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, Seq.empty)(fakeDisplayRequest(request), messages(application)).toString
      }
    }
    /*
    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = userAnswersQtNumber.set(AdditionalUnquotedSharePage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, additionalUnquotedShareRoute)

        val view = application.injector.instanceOf[AdditionalUnquotedShareView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(fakeDisplayRequest(request), messages(application)).toString
      }
    }
     */
    "must redirect to the next page when valid data is submitted" in {
      val application =
        applicationBuilder(userAnswers = Some(userAnswersQtNumber))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, additionalUnquotedShareRoute)
            .withFormUrlEncodedBody(("add-another", "Yes"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.UnquotedShareCompanyNameController.onPageLoad(NormalMode).url
      }
    }
    /*
    "must redirect to NormalMode if changed from false to true in CheckMode" in {
      val previousAnswers = emptyUserAnswers.set(AdditionalUnquotedSharePage, false).success.value
      val application     = applicationBuilder(userAnswers = Some(previousAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, routes.AdditionalUnquotedShareController.onSubmit(CheckMode).url)
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.UnquotedShareCompanyNameController.onPageLoad(NormalMode).url
      }
    }
     */
    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersQtNumber)).build()

      running(application) {
        val request =
          FakeRequest(POST, additionalUnquotedShareRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AdditionalUnquotedShareView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, Seq.empty)(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, additionalUnquotedShareRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, additionalUnquotedShareRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

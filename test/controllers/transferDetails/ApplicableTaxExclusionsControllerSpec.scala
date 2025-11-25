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
import forms.transferDetails.ApplicableTaxExclusionsFormProvider
import models.{AmendCheckMode, ApplicableTaxExclusions, NormalMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.transferDetails.ApplicableTaxExclusionsPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.transferDetails.ApplicableTaxExclusionsView

import scala.concurrent.Future

class ApplicableTaxExclusionsControllerSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  private lazy val applicableTaxExclusionsRoute = routes.ApplicableTaxExclusionsController.onPageLoad(NormalMode).url

  private val formProvider = new ApplicableTaxExclusionsFormProvider()
  private val form         = formProvider()

  "ApplicableTaxExclusions Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder().build()

      running(application) {
        val request = FakeRequest(GET, applicableTaxExclusionsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ApplicableTaxExclusionsView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, NormalMode)(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(ApplicableTaxExclusionsPage, ApplicableTaxExclusions.values.toSet).success.value

      val application = applicationBuilder(userAnswers = userAnswers).build()

      running(application) {
        val request = FakeRequest(GET, applicableTaxExclusionsRoute)

        val view = application.injector.instanceOf[ApplicableTaxExclusionsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(ApplicableTaxExclusions.values.toSet), NormalMode)(
          fakeDisplayRequest(request),
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = emptyUserAnswers)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, applicableTaxExclusionsRoute)
            .withFormUrlEncodedBody(("value[0]", ApplicableTaxExclusions.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual ApplicableTaxExclusionsPage.nextPage(NormalMode, emptyUserAnswers).url
      }
    }

    "must redirect to the next page when valid data is submitted in AmendCheckMode" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder()
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, routes.ApplicableTaxExclusionsController.onPageLoad(AmendCheckMode).url)
            .withFormUrlEncodedBody(("value[0]", ApplicableTaxExclusions.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual ApplicableTaxExclusionsPage.nextPage(AmendCheckMode, emptyUserAnswers).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder().build()

      running(application) {
        val request =
          FakeRequest(POST, applicableTaxExclusionsRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[ApplicableTaxExclusionsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(fakeDisplayRequest(request), messages(application)).toString
      }
    }
  }
}

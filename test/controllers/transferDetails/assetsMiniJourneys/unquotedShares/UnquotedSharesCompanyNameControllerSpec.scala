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

package controllers.transferDetails.assetsMiniJourneys.unquotedShares

import base.SpecBase
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import forms.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesCompanyNameFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesCompanyNamePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesCompanyNameView

import scala.concurrent.Future

class UnquotedSharesCompanyNameControllerSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  private val formProvider = new UnquotedSharesCompanyNameFormProvider()
  private val form         = formProvider()
  private val index        = 0

  private lazy val unquotedSharesCompanyNameRoute = AssetsMiniJourneysRoutes.UnquotedSharesCompanyNameController.onPageLoad(NormalMode, index).url

  "UnquotedSharesCompanyName Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = userAnswersQtNumber).build()

      running(application) {
        val request = FakeRequest(GET, unquotedSharesCompanyNameRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UnquotedSharesCompanyNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, index)(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = userAnswersQtNumber.set(UnquotedSharesCompanyNamePage(index), "answer").success.value

      val application = applicationBuilder(userAnswers = userAnswers).build()

      running(application) {
        val request = FakeRequest(GET, unquotedSharesCompanyNameRoute)

        val view = application.injector.instanceOf[UnquotedSharesCompanyNameView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), NormalMode, index)(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = userAnswersQtNumber)
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, unquotedSharesCompanyNameRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual UnquotedSharesCompanyNamePage(index).nextPage(NormalMode, userAnswersQtNumber).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = userAnswersQtNumber).build()

      running(application) {
        val request =
          FakeRequest(POST, unquotedSharesCompanyNameRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[UnquotedSharesCompanyNameView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, index)(fakeDisplayRequest(request), messages(application)).toString
      }
    }
  }
}

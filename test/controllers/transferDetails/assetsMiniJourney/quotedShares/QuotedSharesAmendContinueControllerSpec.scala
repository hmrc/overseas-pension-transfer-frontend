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

package controllers.transferDetails.assetsMiniJourney.quotedShares

import base.SpecBase
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import forms.transferDetails.assetsMiniJourney.quotedShares.QuotedSharesAmendContinueFormProvider
import models.NormalMode
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.transferDetails.assetsMiniJourney.quotedShares.QuotedSharesAmendContinueView

class QuotedSharesAmendContinueControllerSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  private val formProvider = new QuotedSharesAmendContinueFormProvider()
  private val form         = formProvider()

  private lazy val quotedSharesAmendContinueRoute = AssetsMiniJourneysRoutes.QuotedSharesAmendContinueController.onPageLoad(NormalMode).url

  "QuotedSharesAmendContinue Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersQtNumber)).build()

      running(application) {
        val request = FakeRequest(GET, quotedSharesAmendContinueRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[QuotedSharesAmendContinueView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, Seq.empty, NormalMode)(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val application =
        applicationBuilder(userAnswers = Some(userAnswersQtNumber))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, quotedSharesAmendContinueRoute)
            .withFormUrlEncodedBody(("add-another", "Yes"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual AssetsMiniJourneysRoutes.QuotedSharesCompanyNameController.onPageLoad(NormalMode, 0).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersQtNumber)).build()

      running(application) {
        val request =
          FakeRequest(POST, quotedSharesAmendContinueRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[QuotedSharesAmendContinueView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, Seq.empty, NormalMode)(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, quotedSharesAmendContinueRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, quotedSharesAmendContinueRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

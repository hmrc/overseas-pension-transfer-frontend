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
import controllers.transferDetails.routes
import forms.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesAmendContinueFormProvider
import models.NormalMode
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesAmendContinueView

class UnquotedSharesAmendContinueControllerSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  private val formProvider = new UnquotedSharesAmendContinueFormProvider()
  private val form         = formProvider()

  private lazy val unquotedSharesAmendContinueRoute = AssetsMiniJourneysRoutes.UnquotedSharesAmendContinueController.onPageLoad(NormalMode).url

  "UnquotedSharesAmendContinue Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersQtNumber)).build()

      running(application) {
        val request = FakeRequest(GET, unquotedSharesAmendContinueRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UnquotedSharesAmendContinueView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, Seq.empty, NormalMode)(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must redirect to unquoted shares company name when add-another selected" in {
      val application =
        applicationBuilder(userAnswers = Some(userAnswersQtNumber))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, unquotedSharesAmendContinueRoute)
            .withFormUrlEncodedBody(("add-another", "Yes"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual AssetsMiniJourneysRoutes.UnquotedSharesCompanyNameController.onPageLoad(NormalMode, 0).url
      }
    }

    "must redirect to transfer detail cya page when add-another not selected and no more assets to add" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersQtNumber))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, unquotedSharesAmendContinueRoute)
            .withFormUrlEncodedBody(("add-another", "No"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.TransferDetailsCYAController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersQtNumber)).build()

      running(application) {
        val request =
          FakeRequest(POST, unquotedSharesAmendContinueRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[UnquotedSharesAmendContinueView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, Seq.empty, NormalMode)(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, unquotedSharesAmendContinueRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, unquotedSharesAmendContinueRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

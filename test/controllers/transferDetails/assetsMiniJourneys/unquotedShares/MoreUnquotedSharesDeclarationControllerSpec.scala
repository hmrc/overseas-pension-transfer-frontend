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
import controllers.routes
import forms.transferDetails.assetsMiniJourneys.unquotedShares.MoreUnquotedSharesDeclarationFormProvider
import models.NormalMode
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.transferDetails.assetsMiniJourneys.unquotedShares.MoreUnquotedSharesDeclarationPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesAmendContinueSummary
import views.html.transferDetails.assetsMiniJourneys.unquotedShares.MoreUnquotedSharesDeclarationView

class MoreUnquotedSharesDeclarationControllerSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  private val formProvider = new MoreUnquotedSharesDeclarationFormProvider()
  private val form         = formProvider()

  private lazy val moreUnquotedSharesDeclarationRoute =
    controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes.MoreUnquotedSharesDeclarationController.onPageLoad(NormalMode).url

  "MoreUnquotedSharesDeclaration Controller" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithAssets(assetsCount = 5))).build()
      running(application) {
        val request = FakeRequest(GET, moreUnquotedSharesDeclarationRoute)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[MoreUnquotedSharesDeclarationView]

        val rows = UnquotedSharesAmendContinueSummary.rows(userAnswersWithAssets(assetsCount = 5))

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, rows, NormalMode)(fakeDisplayRequest(request, userAnswersWithAssets(assetsCount = 5)), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = userAnswersQtNumber.set(MoreUnquotedSharesDeclarationPage, true).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, moreUnquotedSharesDeclarationRoute)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[MoreUnquotedSharesDeclarationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), Seq.empty, NormalMode)(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must redirect to CYA page when valid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithAssets(assetsCount = 5))).build()

      running(application) {
        val request =
          FakeRequest(POST, moreUnquotedSharesDeclarationRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.transferDetails.routes.TransferDetailsCYAController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersQtNumber)).build()

      running(application) {
        val request =
          FakeRequest(POST, moreUnquotedSharesDeclarationRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[MoreUnquotedSharesDeclarationView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, Seq.empty, NormalMode)(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, moreUnquotedSharesDeclarationRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, moreUnquotedSharesDeclarationRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

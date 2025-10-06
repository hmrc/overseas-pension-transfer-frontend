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

package controllers.transferDetails.assetsMiniJourneys.otherAssets

import base.SpecBase
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import forms.transferDetails.assetsMiniJourneys.otherAssets.OtherAssetsConfirmRemovalFormProvider
import models.NormalMode
import models.assets.OtherAssetsEntry
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.assets.OtherAssetsQuery
import views.html.transferDetails.assetsMiniJourneys.otherAssets.OtherAssetsConfirmRemovalView

class OtherAssetsConfirmRemovalControllerSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  private val formProvider = new OtherAssetsConfirmRemovalFormProvider()
  private val form         = formProvider()

  "OtherAssetsConfirmRemoval Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder().build()

      running(application) {
        val request = FakeRequest(GET, AssetsMiniJourneysRoutes.OtherAssetsConfirmRemovalController.onPageLoad(1).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[OtherAssetsConfirmRemovalView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, 1)(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val entries     = List(OtherAssetsEntry("Other", 1000))
      val userAnswers = emptyUserAnswers.set(OtherAssetsQuery, entries).success.value

      val application = applicationBuilder(userAnswers = userAnswers).build()

      running(application) {
        val request =
          FakeRequest(POST, AssetsMiniJourneysRoutes.OtherAssetsConfirmRemovalController.onPageLoad(0).url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual AssetsMiniJourneysRoutes.OtherAssetsAmendContinueController.onPageLoad(NormalMode).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(sessionData = sessionDataQtNumber).build()

      running(application) {
        val request =
          FakeRequest(POST, AssetsMiniJourneysRoutes.OtherAssetsConfirmRemovalController.onPageLoad(1).url)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[OtherAssetsConfirmRemovalView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, 1)(fakeDisplayRequest(request), messages(application)).toString
      }
    }
  }
}

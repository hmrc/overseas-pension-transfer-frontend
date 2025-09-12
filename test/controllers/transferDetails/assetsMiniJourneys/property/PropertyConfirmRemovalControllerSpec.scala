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

package controllers.transferDetails.assetsMiniJourneys.property

import base.AddressBase
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import forms.transferDetails.assetsMiniJourneys.property.PropertyConfirmRemovalFormProvider
import models.NormalMode
import models.assets.PropertyEntry
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.assets.PropertyQuery
import views.html.transferDetails.assetsMiniJourneys.property.PropertyConfirmRemovalView

class PropertyConfirmRemovalControllerSpec extends AnyFreeSpec with AddressBase with MockitoSugar {

  private val formProvider = new PropertyConfirmRemovalFormProvider()
  private val form         = formProvider()

  "PropertyConfirmRemoval Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = userAnswersQtNumber).build()

      running(application) {
        val request = FakeRequest(GET, AssetsMiniJourneysRoutes.PropertyConfirmRemovalController.onPageLoad(1).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PropertyConfirmRemovalView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, 1)(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val entries     = List(PropertyEntry(propertyAddress, 1000, "description"))
      val userAnswers = userAnswersQtNumber.set(PropertyQuery, entries).success.value

      val application = applicationBuilder(userAnswers = userAnswers).build()

      running(application) {
        val request =
          FakeRequest(POST, AssetsMiniJourneysRoutes.PropertyConfirmRemovalController.onPageLoad(0).url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual AssetsMiniJourneysRoutes.PropertyAmendContinueController.onPageLoad(NormalMode).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = userAnswersQtNumber).build()

      running(application) {
        val request =
          FakeRequest(POST, AssetsMiniJourneysRoutes.PropertyConfirmRemovalController.onPageLoad(1).url)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[PropertyConfirmRemovalView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, 1)(fakeDisplayRequest(request), messages(application)).toString
      }
    }
  }
}

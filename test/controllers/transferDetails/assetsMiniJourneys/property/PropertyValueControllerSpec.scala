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

import base.SpecBase
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import forms.transferDetails.assetsMiniJourneys.property.PropertyValueFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.transferDetails.assetsMiniJourneys.property.PropertyValuePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.transferDetails.assetsMiniJourneys.property.PropertyValueView

import scala.concurrent.Future

class PropertyValueControllerSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  val formProvider  = new PropertyValueFormProvider()
  val form          = formProvider()
  private val index = 0

  val validAnswer = BigDecimal(0.01)

  lazy val propertyValueRoute = AssetsMiniJourneysRoutes.PropertyValueController.onPageLoad(NormalMode, index).url

  "ValueOfThisProperty Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(sessionData = sessionDataQtNumber).build()

      running(application) {
        val request = FakeRequest(GET, propertyValueRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PropertyValueView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, index)(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val sessionData = sessionDataQtNumber.set(PropertyValuePage(index), validAnswer).success.value

      val application = applicationBuilder(sessionData = sessionData).build()

      running(application) {
        val request = FakeRequest(GET, propertyValueRoute)

        val view = application.injector.instanceOf[PropertyValueView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode, index)(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(sessionData = sessionDataQtNumber)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, propertyValueRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual PropertyValuePage(index).nextPage(NormalMode, emptyUserAnswers).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(sessionData = sessionDataQtNumber).build()

      running(application) {
        val request =
          FakeRequest(POST, propertyValueRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[PropertyValueView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, index)(fakeDisplayRequest(request), messages(application)).toString
      }
    }
  }
}

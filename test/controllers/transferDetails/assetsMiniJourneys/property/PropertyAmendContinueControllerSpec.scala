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
import forms.transferDetails.assetsMiniJourneys.property.PropertyAmendContinueFormProvider
import models.assets.{PropertyEntry, PropertyMiniJourney}
import models.{CheckMode, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.transferDetails.assetsMiniJourneys.property.PropertyAmendContinuePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.TransferDetailsService
import views.html.transferDetails.assetsMiniJourneys.property.PropertyAmendContinueView

import scala.concurrent.Future

class PropertyAmendContinueControllerSpec extends AnyFreeSpec with AddressBase with MockitoSugar {

  private val formProvider = new PropertyAmendContinueFormProvider()
  private val form         = formProvider()

  private lazy val propertyAmendContinueRouteNormal =
    AssetsMiniJourneysRoutes.PropertyAmendContinueController.onPageLoad(NormalMode).url

  private lazy val propertyAmendContinueRouteCheck =
    AssetsMiniJourneysRoutes.PropertyAmendContinueController.onPageLoad(CheckMode).url

  private def uaWithProperties(n: Int): UserAnswers = {
    val entry = PropertyEntry(
      propertyAddress = propertyAddress,
      propValue       = BigDecimal(100000),
      propDescription = "Test property"
    )
    val list  = List.fill(n)(entry)
    emptyUserAnswers.set(PropertyMiniJourney.query, list).success.value
  }

  "PropertyAmendContinue Controller" - {

    "must return OK and the correct view for a GET in NormalMode" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersQtNumber)).build()

      running(application) {
        val request = FakeRequest(GET, propertyAmendContinueRouteNormal)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[PropertyAmendContinueView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, Seq.empty, NormalMode)(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must return OK and the form filled for a GET in NormalMode when answer exists" in {
      val userAnswers = userAnswersQtNumber.set(PropertyAmendContinuePage, true).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, propertyAmendContinueRouteNormal)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[PropertyAmendContinueView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), Seq.empty, NormalMode)(fakeDisplayRequest(request, userAnswers), messages(application)).toString
      }
    }

    "must return OK for a GET in CheckMode and save completion" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val userAnswers = uaWithProperties(1)
      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request = FakeRequest(GET, propertyAmendContinueRouteCheck)
        val result  = route(application, request).value
        status(result) mustEqual OK
      }
    }

    "must redirect to the page's nextPageWith when valid data 'Yes' is submitted in NormalMode" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val userAnswers = uaWithProperties(2)
      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, propertyAmendContinueRouteNormal)
            .withFormUrlEncodedBody(("add-another", "Yes"))

        val result = route(application, request).value

        val ua2       = userAnswers.set(PropertyAmendContinuePage, true).success.value
        val nextIndex = TransferDetailsService.assetCount(PropertyMiniJourney, ua2)
        val expected  = PropertyAmendContinuePage.nextPageWith(NormalMode, ua2, nextIndex).url

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expected
      }
    }

    "must redirect to the page's nextPageWith when valid data 'No' is submitted in NormalMode" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val userAnswers = uaWithProperties(0)
      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, propertyAmendContinueRouteNormal)
            .withFormUrlEncodedBody(("add-another", "No"))

        val result = route(application, request).value

        val ua2       = userAnswers.set(PropertyAmendContinuePage, false).success.value
        val nextIndex = TransferDetailsService.assetCount(PropertyMiniJourney, ua2)
        val expected  = PropertyAmendContinuePage.nextPageWith(NormalMode, ua2, nextIndex).url

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expected
      }
    }

    "must redirect to CYA when valid data is submitted in CheckMode" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val userAnswers = uaWithProperties(3)
      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, AssetsMiniJourneysRoutes.PropertyAmendContinueController.onSubmit(CheckMode).url)
            .withFormUrlEncodedBody(("add-another", "Yes"))

        val result = route(application, request).value

        val ua2       = userAnswers.set(PropertyAmendContinuePage, true).success.value
        val nextIndex = TransferDetailsService.assetCount(PropertyMiniJourney, ua2)
        val expected  = PropertyAmendContinuePage.nextPageWith(CheckMode, ua2, nextIndex).url

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expected
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersQtNumber)).build()

      running(application) {
        val request =
          FakeRequest(POST, propertyAmendContinueRouteNormal)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view   = application.injector.instanceOf[PropertyAmendContinueView]
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, Seq.empty, NormalMode)(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, propertyAmendContinueRouteNormal)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, propertyAmendContinueRouteNormal)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

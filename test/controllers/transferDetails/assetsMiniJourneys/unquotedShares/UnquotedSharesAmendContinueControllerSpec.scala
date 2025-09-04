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
import forms.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesAmendContinueFormProvider
import models.assets.{UnquotedSharesEntry, UnquotedSharesMiniJourney}
import models.{CheckMode, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesAmendContinuePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.TransferDetailsService
import views.html.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesAmendContinueView

import scala.concurrent.Future

class UnquotedSharesAmendContinueControllerSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  private val formProvider = new UnquotedSharesAmendContinueFormProvider()
  private val form         = formProvider()

  private lazy val unquotedSharesAmendContinueRouteNormal =
    AssetsMiniJourneysRoutes.UnquotedSharesAmendContinueController.onPageLoad(NormalMode).url

  private lazy val unquotedSharesAmendContinueRouteCheck =
    AssetsMiniJourneysRoutes.UnquotedSharesAmendContinueController.onPageLoad(CheckMode).url

  private def uaWithUnquotedShares(n: Int): UserAnswers = {
    val entry = UnquotedSharesEntry(
      companyName    = "Acme Ltd",
      valueOfShares  = BigDecimal(1234.56),
      numberOfShares = 100,
      classOfShares  = "Ord"
    )
    val list  = List.fill(n)(entry)
    emptyUserAnswers.set(UnquotedSharesMiniJourney.query, list).success.value
  }

  "UnquotedSharesAmendContinue Controller" - {

    "must return OK and the correct view for a GET in NormalMode" in {
      val application = applicationBuilder(userAnswers = userAnswersQtNumber).build()

      running(application) {
        val request = FakeRequest(GET, unquotedSharesAmendContinueRouteNormal)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[UnquotedSharesAmendContinueView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, Seq.empty, NormalMode)(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must return OK and the form filled for a GET in NormalMode when answer exists" in {
      val userAnswers = userAnswersQtNumber.set(UnquotedSharesAmendContinuePage, value = true).success.value
      val application = applicationBuilder(userAnswers = userAnswers).build()

      running(application) {
        val request = FakeRequest(GET, unquotedSharesAmendContinueRouteNormal)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[UnquotedSharesAmendContinueView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), Seq.empty, NormalMode)(fakeDisplayRequest(request, userAnswers), messages(application)).toString
      }
    }

    "must return OK for a GET in CheckMode and save completion" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val userAnswers = uaWithUnquotedShares(1)
      val application =
        applicationBuilder(userAnswers = userAnswers)
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request = FakeRequest(GET, unquotedSharesAmendContinueRouteCheck)
        val result  = route(application, request).value
        status(result) mustEqual OK
      }
    }

    "must redirect to the page's nextPageWith when valid data 'Yes' is submitted in NormalMode" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val userAnswers = uaWithUnquotedShares(2)
      val application =
        applicationBuilder(userAnswers = userAnswers)
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, unquotedSharesAmendContinueRouteNormal)
            .withFormUrlEncodedBody(("add-another", "Yes"))

        val result = route(application, request).value

        val ua2       = userAnswers.set(UnquotedSharesAmendContinuePage, value = true).success.value
        val nextIndex = TransferDetailsService.assetCount(UnquotedSharesMiniJourney, ua2)
        val expected  = UnquotedSharesAmendContinuePage.nextPageWith(NormalMode, ua2, nextIndex).url

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expected
      }
    }

    "must redirect to the page's nextPageWith when valid data 'No' is submitted in NormalMode" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val userAnswers = uaWithUnquotedShares(0)
      val application =
        applicationBuilder(userAnswers = userAnswers)
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, unquotedSharesAmendContinueRouteNormal)
            .withFormUrlEncodedBody(("add-another", "No"))

        val result = route(application, request).value

        val ua2       = userAnswers.set(UnquotedSharesAmendContinuePage, value = false).success.value
        val nextIndex = TransferDetailsService.assetCount(UnquotedSharesMiniJourney, ua2)
        val expected  = UnquotedSharesAmendContinuePage.nextPageWith(NormalMode, ua2, nextIndex).url

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expected
      }
    }

    "must redirect to CYA when valid data is submitted in CheckMode" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val userAnswers = uaWithUnquotedShares(3)
      val application =
        applicationBuilder(userAnswers = userAnswers)
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, AssetsMiniJourneysRoutes.UnquotedSharesAmendContinueController.onSubmit(CheckMode).url)
            .withFormUrlEncodedBody(("add-another", "Yes"))

        val result = route(application, request).value

        val ua2       = userAnswers.set(UnquotedSharesAmendContinuePage, value = true).success.value
        val nextIndex = TransferDetailsService.assetCount(UnquotedSharesMiniJourney, ua2)
        val expected  = UnquotedSharesAmendContinuePage.nextPageWith(CheckMode, ua2, nextIndex).url

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expected
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = userAnswersQtNumber).build()

      running(application) {
        val request =
          FakeRequest(POST, unquotedSharesAmendContinueRouteNormal)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view   = application.injector.instanceOf[UnquotedSharesAmendContinueView]
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, Seq.empty, NormalMode)(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = emptyUserAnswers).build()

      running(application) {
        val request = FakeRequest(GET, unquotedSharesAmendContinueRouteNormal)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = emptyUserAnswers).build()

      running(application) {
        val request =
          FakeRequest(POST, unquotedSharesAmendContinueRouteNormal)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

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

package controllers.transferDetails.assetsMiniJourneys.quotedShares

import base.SpecBase
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import forms.transferDetails.assetsMiniJourneys.quotedShares.QuotedSharesAmendContinueFormProvider
import models.assets.{QuotedSharesEntry, QuotedSharesMiniJourney, TypeOfAsset}
import models.{CheckMode, NormalMode}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.transferDetails.assetsMiniJourneys.quotedShares.QuotedSharesAmendContinuePage
import play.api.inject.bind
import play.api.libs.json.Reads
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.TransferDetailsService
import views.html.transferDetails.assetsMiniJourneys.quotedShares.QuotedSharesAmendContinueView

import scala.concurrent.Future
import scala.util.Success

class QuotedSharesAmendContinueControllerSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  private val formProvider = new QuotedSharesAmendContinueFormProvider()
  private val form         = formProvider()

  private lazy val quotedSharesAmendContinueRouteNormal = AssetsMiniJourneysRoutes.QuotedSharesAmendContinueController.onPageLoad(NormalMode).url
  private lazy val quotedSharesAmendContinueRouteCheck  = AssetsMiniJourneysRoutes.QuotedSharesAmendContinueController.onPageLoad(CheckMode).url

  "QuotedSharesAmendContinue Controller" - {

    "must return OK and the correct view for a GET in NormalMode" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersQtNumber)).build()

      running(application) {
        val request = FakeRequest(GET, quotedSharesAmendContinueRouteNormal)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[QuotedSharesAmendContinueView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, Seq.empty, NormalMode)(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must return OK and the form filled for a GET in NormalMode when answer exists" in {
      val userAnswers = userAnswersQtNumber.set(QuotedSharesAmendContinuePage, true).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, quotedSharesAmendContinueRouteNormal)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[QuotedSharesAmendContinueView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), Seq.empty, NormalMode)(fakeDisplayRequest(request, userAnswers), messages(application)).toString
      }
    }

    "must return OK for a GET in CheckMode and save completion" in {
      val mockSessionRepository      = mock[SessionRepository]
      val mockTransferDetailsService = mock[TransferDetailsService]

      when(mockTransferDetailsService.setAssetCompleted(any(), eqTo(TypeOfAsset.QuotedShares), eqTo(true)))
        .thenReturn(Success(emptyUserAnswers))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[TransferDetailsService].toInstance(mockTransferDetailsService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, quotedSharesAmendContinueRouteCheck)
        val result  = route(application, request).value
        status(result) mustEqual OK
      }
    }

    "must redirect to the page's nextPageWith when valid data 'Yes' is submitted in NormalMode" in {
      val mockSessionRepository      = mock[SessionRepository]
      val mockTransferDetailsService = mock[TransferDetailsService]
      val nextIndex                  = 2

      when(mockTransferDetailsService.setAssetCompleted(any(), eqTo(TypeOfAsset.QuotedShares), eqTo(true)))
        .thenReturn(Success(emptyUserAnswers))
      when(
        mockTransferDetailsService
          .assetCount(eqTo(QuotedSharesMiniJourney), any[models.UserAnswers])(any[Reads[QuotedSharesEntry]])
      ).thenReturn(nextIndex)
      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[TransferDetailsService].toInstance(mockTransferDetailsService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, quotedSharesAmendContinueRouteNormal)
            .withFormUrlEncodedBody(("add-another", "Yes"))

        val result = route(application, request).value

        val ua2      = emptyUserAnswers.set(QuotedSharesAmendContinuePage, true).success.value
        val expected = QuotedSharesAmendContinuePage.nextPageWith(NormalMode, ua2, nextIndex).url

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expected
      }
    }

    "must redirect to the page's nextPageWith when valid data 'No' is submitted in NormalMode" in {
      val mockSessionRepository      = mock[SessionRepository]
      val mockTransferDetailsService = mock[TransferDetailsService]
      val nextIndex                  = 0

      when(mockTransferDetailsService.setAssetCompleted(any(), eqTo(TypeOfAsset.QuotedShares), eqTo(true)))
        .thenReturn(Success(emptyUserAnswers))
      when(
        mockTransferDetailsService
          .assetCount(eqTo(QuotedSharesMiniJourney), any[models.UserAnswers])(any[Reads[QuotedSharesEntry]])
      ).thenReturn(nextIndex)
      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[TransferDetailsService].toInstance(mockTransferDetailsService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, quotedSharesAmendContinueRouteNormal)
            .withFormUrlEncodedBody(("add-another", "No"))

        val result = route(application, request).value

        val ua2      = emptyUserAnswers.set(QuotedSharesAmendContinuePage, false).success.value
        val expected = QuotedSharesAmendContinuePage.nextPageWith(NormalMode, ua2, nextIndex).url

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expected
      }
    }

    "must redirect to CYA when valid data is submitted in CheckMode" in {
      val mockSessionRepository      = mock[SessionRepository]
      val mockTransferDetailsService = mock[TransferDetailsService]
      val nextIndex                  = 3

      when(mockTransferDetailsService.setAssetCompleted(any(), eqTo(TypeOfAsset.QuotedShares), eqTo(true)))
        .thenReturn(Success(emptyUserAnswers))
      when(
        mockTransferDetailsService
          .assetCount(eqTo(QuotedSharesMiniJourney), any[models.UserAnswers])(any[Reads[QuotedSharesEntry]])
      ).thenReturn(nextIndex)
      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[TransferDetailsService].toInstance(mockTransferDetailsService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, AssetsMiniJourneysRoutes.QuotedSharesAmendContinueController.onSubmit(CheckMode).url)
            .withFormUrlEncodedBody(("add-another", "Yes"))

        val result = route(application, request).value

        val ua2      = emptyUserAnswers.set(QuotedSharesAmendContinuePage, true).success.value
        val expected = QuotedSharesAmendContinuePage.nextPageWith(CheckMode, ua2, nextIndex).url

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expected
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersQtNumber)).build()

      running(application) {
        val request =
          FakeRequest(POST, quotedSharesAmendContinueRouteNormal)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view   = application.injector.instanceOf[QuotedSharesAmendContinueView]
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, Seq.empty, NormalMode)(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, quotedSharesAmendContinueRouteNormal)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, quotedSharesAmendContinueRouteNormal)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

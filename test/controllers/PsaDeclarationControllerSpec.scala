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

package controllers

import base.SpecBase
import models.{NormalMode, QtNumber}
import models.responses.{SubmissionErrorResponse, SubmissionResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.PsaDeclarationPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.UserAnswersService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.PsaDeclarationView

import scala.concurrent.Future

class PsaDeclarationControllerSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private lazy val psaDeclarationRoute = routes.PsaDeclarationController.onPageLoad().url

  "PsaDeclaration Controller" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = userAnswersQtNumber).build()

      running(application) {
        val request = FakeRequest(GET, psaDeclarationRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PsaDeclarationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must redirect to the next page when submitted" in {
      val mockUserAnswersService = mock[UserAnswersService]
      val mockSessionRepository  = mock[SessionRepository]

      when(mockUserAnswersService.submitDeclaration(any(), any(), any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(SubmissionResponse(QtNumber("QT123456")))))

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = userAnswersQtNumber)
          .overrides(
            bind[UserAnswersService].toInstance(mockUserAnswersService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request = FakeRequest(POST, psaDeclarationRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual PsaDeclarationPage.nextPage(NormalMode, emptyUserAnswers).url
      }
    }

    "must redirect to Journey Recovery when submission fails" in {
      val mockUserAnswersService = mock[UserAnswersService]

      when(mockUserAnswersService.submitDeclaration(any(), any(), any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(SubmissionErrorResponse("boom", None))))

      val application =
        applicationBuilder(userAnswers = userAnswersQtNumber)
          .overrides(bind[UserAnswersService].toInstance(mockUserAnswersService))
          .build()

      running(application) {
        val request = FakeRequest(POST, psaDeclarationRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = emptyUserAnswers).build()

      running(application) {
        val request = FakeRequest(GET, psaDeclarationRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = emptyUserAnswers).build()

      running(application) {
        val request = FakeRequest(POST, psaDeclarationRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

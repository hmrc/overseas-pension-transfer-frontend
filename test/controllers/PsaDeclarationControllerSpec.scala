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
import connectors.{DetailsNotFound, MinimalDetailsConnector}
import models.authentication.PsaId
import models.responses.{SubmissionErrorResponse, SubmissionResponse}
import models.{MinimalDetails, NormalMode, QtNumber, SessionData}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.PsaDeclarationPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.{EmailSentSuccess, EmailService, UserAnswersService}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.PsaDeclarationView

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

class PsaDeclarationControllerSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private lazy val psaDeclarationRoute = routes.PsaDeclarationController.onPageLoad(NormalMode).url

  "PsaDeclaration Controller" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder().build()

      running(application) {
        val request = FakeRequest(GET, psaDeclarationRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PsaDeclarationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(NormalMode)(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must redirect to the next page when submitted" in {
      val mockUserAnswersService      = mock[UserAnswersService]
      val mockSessionRepository       = mock[SessionRepository]
      val mockMinimalDetailsConnector = mock[MinimalDetailsConnector]
      val mockEmailService            = mock[EmailService]

      val receiptDate    = Instant.now
      val qtNumber       = QtNumber("QT123456")
      val minimalDetails = mock[MinimalDetails]

      when(mockUserAnswersService.submitDeclaration(any(), any(), any(), any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(SubmissionResponse(qtNumber, receiptDate))))

      when(mockMinimalDetailsConnector.fetch(any[PsaId]())(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(Right(minimalDetails)))

      when(mockEmailService.sendConfirmationEmail(any[SessionData], any[MinimalDetails])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(EmailSentSuccess)))

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder()
          .overrides(
            bind[UserAnswersService].toInstance(mockUserAnswersService),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[MinimalDetailsConnector].toInstance(mockMinimalDetailsConnector),
            bind[EmailService].toInstance(mockEmailService)
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

      when(mockUserAnswersService.submitDeclaration(any(), any(), any(), any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(SubmissionErrorResponse("boom", None))))

      val application =
        applicationBuilder()
          .overrides(bind[UserAnswersService].toInstance(mockUserAnswersService))
          .build()

      running(application) {
        val request = FakeRequest(POST, psaDeclarationRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when minimal details lookup returns an error" in {
      val mockUserAnswersService      = mock[UserAnswersService]
      val mockMinimalDetailsConnector = mock[MinimalDetailsConnector]
      val mockEmailService            = mock[EmailService]
      val mockSessionRepository       = mock[SessionRepository]

      val receiptDate = Instant.now
      val qtNumber    = QtNumber("QT123456")

      when(mockUserAnswersService.submitDeclaration(any(), any(), any(), any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(SubmissionResponse(qtNumber, receiptDate))))

      when(mockMinimalDetailsConnector.fetch(any[PsaId]())(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(Left(DetailsNotFound)))

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder()
          .overrides(
            bind[UserAnswersService].toInstance(mockUserAnswersService),
            bind[MinimalDetailsConnector].toInstance(mockMinimalDetailsConnector),
            bind[EmailService].toInstance(mockEmailService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request = FakeRequest(POST, psaDeclarationRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

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

package controllers.transferDetails

import base.SpecBase
import controllers.routes.JourneyRecoveryController
import forms.transferDetails.DateOfTransferFormProvider
import models.{AmendCheckMode, NormalMode}
import models.responses.UserAnswersErrorResponse
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.transferDetails.DateOfTransferPage
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.UserAnswersService
import views.html.transferDetails.DateOfTransferView

import java.time.{LocalDate, ZoneOffset}
import scala.concurrent.Future

class DateOfTransferControllerSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  implicit private val messages: Messages = stubMessages()

  private val formProvider = new DateOfTransferFormProvider()
  private def form         = formProvider()

  private val validAnswer              = LocalDate.now(ZoneOffset.UTC)
  private lazy val dateOfTransferRoute = routes.DateOfTransferController.onPageLoad(NormalMode).url

  def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, dateOfTransferRoute)

  def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, dateOfTransferRoute)
      .withFormUrlEncodedBody(
        "value.day"   -> validAnswer.getDayOfMonth.toString,
        "value.month" -> validAnswer.getMonthValue.toString,
        "value.year"  -> validAnswer.getYear.toString
      )

  "DateOfTransfer Controller" - {

    "onPageLoad (GET)" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder().build()

        running(application) {
          val result  = route(application, getRequest()).value
          val request = getRequest()
          val view    = application.injector.instanceOf[DateOfTransferView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode)(fakeDisplayRequest(request), messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = emptyUserAnswers.set(DateOfTransferPage, validAnswer).success.value

        val application = applicationBuilder(userAnswers = userAnswers).build()

        running(application) {
          val view    = application.injector.instanceOf[DateOfTransferView]
          val request = getRequest()
          val result  = route(application, getRequest()).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode)(fakeDisplayRequest(request), messages(application)).toString
        }
      }

      "must return OK and use the amend form when in AmendCheckMode" in {
        val originalDate = LocalDate.of(2025, 1, 25)

        val originalSubmission = emptyUserAnswers
          .set(DateOfTransferPage, originalDate).success.value

        val currentUserAnswers = emptyUserAnswers
          .set(DateOfTransferPage, originalDate.minusDays(5)).success.value

        val mockUserAnswersService = mock[UserAnswersService]
        when(mockUserAnswersService.getExternalUserAnswers(any(), any(), any(), any())(any()))
          .thenReturn(Future.successful(Right(originalSubmission)))

        val application = applicationBuilder(userAnswers = currentUserAnswers)
          .overrides(
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.DateOfTransferController.onPageLoad(AmendCheckMode).url)
          val result  = route(application, request).value
          val view    = application.injector.instanceOf[DateOfTransferView]

          val amendFormProvider = application.injector.instanceOf[forms.transferDetails.AmendDateOfTransferFormProvider]
          val amendForm         = amendFormProvider(originalDate)

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(amendForm.fill(originalDate.minusDays(5)), AmendCheckMode, isAmend = true)(
            fakeDisplayRequest(request),
            messages(application)
          ).toString
        }
      }
    }

    "onSubmit (POST)" - {

      "must redirect to the next page when valid data is submitted" in {
        val mockUserAnswersService = mock[UserAnswersService]
        val mockSessionRepository  = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        when(mockUserAnswersService.setExternalUserAnswers(any())(any()))
          .thenReturn(Future.successful(Right(Done)))

        val application = applicationBuilder(userAnswersMemberNameQtNumber)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          )
          .build()

        running(application) {
          val result = route(application, postRequest()).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual DateOfTransferPage.nextPage(NormalMode, emptyUserAnswers).url
        }
      }

      "must redirect to the next page when valid data is submitted in AmendCheckMode" in {
        val mockUserAnswersService = mock[UserAnswersService]

        when(mockUserAnswersService.getExternalUserAnswers(any(), any(), any(), any())(any()))
          .thenReturn(Future.successful(Right(emptyUserAnswers.set(DateOfTransferPage, validAnswer).success.value)))

        when(mockUserAnswersService.setExternalUserAnswers(any())(any()))
          .thenReturn(Future.successful(Right(Done)))

        val application = applicationBuilder(userAnswersMemberNameQtNumber)
          .overrides(
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          )
          .build()

        running(application) {
          val request =
            FakeRequest(POST, routes.DateOfTransferController.onSubmit(AmendCheckMode).url)
              .withFormUrlEncodedBody(
                "value.day"   -> validAnswer.minusDays(1).getDayOfMonth.toString,
                "value.month" -> validAnswer.minusDays(1).getMonthValue.toString,
                "value.year"  -> validAnswer.minusDays(1).getYear.toString
              )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual DateOfTransferPage.nextPage(AmendCheckMode, emptyUserAnswers).url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder().build()

        val request =
          FakeRequest(POST, dateOfTransferRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        running(application) {
          val boundForm = form.bind(Map("value" -> "invalid value"))

          val view = application.injector.instanceOf[DateOfTransferView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode)(fakeDisplayRequest(request), messages(application)).toString
        }
      }

      "must return a Bad Request when amending with a date after the original submission date" in {
        val originalDate = LocalDate.of(2025, 1, 25)
        val newDate      = originalDate.plusDays(1)

        val originalSubmission = emptyUserAnswers
          .set(DateOfTransferPage, originalDate).success.value

        val mockUserAnswersService = mock[UserAnswersService]
        when(mockUserAnswersService.setExternalUserAnswers(any())(any()))
          .thenReturn(Future.successful(Right(Done)))

        when(mockUserAnswersService.getExternalUserAnswers(any(), any(), any(), any())(any()))
          .thenReturn(Future.successful(Right(originalSubmission)))

        val application = applicationBuilder(userAnswers = emptyUserAnswers)
          .overrides(
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          )
          .build()

        running(application) {
          val request =
            FakeRequest(POST, routes.DateOfTransferController.onSubmit(models.AmendCheckMode).url)
              .withFormUrlEncodedBody(
                "value.day"   -> newDate.getDayOfMonth.toString,
                "value.month" -> newDate.getMonthValue.toString,
                "value.year"  -> newDate.getYear.toString
              )

          val result = route(application, request).value
          status(result) mustEqual BAD_REQUEST

          contentAsString(result) must include(s"Date of transfer must be before your original submission date")
        }
      }

      "must redirect to JourneyRecovery for a POST when userAnswersService returns a Left" in {
        val mockUserAnswersService = mock[UserAnswersService]
        val mockSessionRepository  = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        when(mockUserAnswersService.setExternalUserAnswers(any())(any()))
          .thenReturn(Future.successful(Left(UserAnswersErrorResponse("Error", None))))

        val application = applicationBuilder(userAnswersMemberNameQtNumber)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          )
          .build()

        running(application) {
          val result = route(application, postRequest()).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}

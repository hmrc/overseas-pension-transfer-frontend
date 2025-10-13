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
import models.NormalMode
import models.responses.UserAnswersErrorResponse
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.transferDetails.DateOfTransferPage
import play.api.i18n.Messages
import play.api.inject.bind
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

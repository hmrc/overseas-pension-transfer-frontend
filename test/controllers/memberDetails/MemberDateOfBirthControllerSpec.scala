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

package controllers.memberDetails

import base.SpecBase
import controllers.routes.JourneyRecoveryController
import forms.memberDetails.MemberDateOfBirthFormProvider
import models.{CheckMode, NormalMode}
import models.responses.UserAnswersErrorResponse
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.memberDetails.MemberDateOfBirthPage
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.UserAnswersService
import views.html.memberDetails.MemberDateOfBirthView

import java.time.{LocalDate, ZoneOffset}
import scala.concurrent.Future

class MemberDateOfBirthControllerSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  implicit private val messages: Messages = stubMessages()

  private val formProvider = new MemberDateOfBirthFormProvider()
  private def form         = formProvider()

  private val validAnswer                     = LocalDate.now(ZoneOffset.UTC)
  private lazy val memberDateOfBirthGetRoute  = routes.MemberDateOfBirthController.onPageLoad(NormalMode).url
  private lazy val memberDateOfBirthPostRoute = routes.MemberDateOfBirthController.onSubmit(NormalMode, fromFinalCYA = false).url

  def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, memberDateOfBirthGetRoute)

  def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, memberDateOfBirthPostRoute)
      .withFormUrlEncodedBody(
        "value.day"   -> validAnswer.getDayOfMonth.toString,
        "value.month" -> validAnswer.getMonthValue.toString,
        "value.year"  -> validAnswer.getYear.toString
      )

  "MemberDateOfBirth Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersMemberNameQtNumber)).build()

      running(application) {
        val request = getRequest()
        val result  = route(application, request).value

        val view = application.injector.instanceOf[MemberDateOfBirthView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, false)(
          fakeDisplayRequest(request),
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = userAnswersMemberNameQtNumber.set(MemberDateOfBirthPage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = getRequest()
        val view    = application.injector.instanceOf[MemberDateOfBirthView]
        val result  = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode, false)(
          fakeDisplayRequest(request, userAnswers),
          messages(application)
        ).toString
      }
    }

    "must redirect to the members current address when valid data is submitted" in {
      val mockUserAnswersService = mock[UserAnswersService]
      val mockSessionRepository  = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockUserAnswersService.setExternalUserAnswers(any())(any()))
        .thenReturn(Future.successful(Right(Done)))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          )
          .build()

      running(application) {
        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual MemberDateOfBirthPage.nextPage(NormalMode, emptyUserAnswers).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersMemberNameQtNumber)).build()

      val request = FakeRequest(POST, memberDateOfBirthPostRoute)
        .withFormUrlEncodedBody(("value", "invalid value"))

      running(application) {
        val boundForm = form.bind(Map("value" -> "invalid value"))
        val view      = application.injector.instanceOf[MemberDateOfBirthView]
        val result    = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, false)(
          fakeDisplayRequest(request),
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val result = route(application, getRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery for a POST when userAnswersService returns a Left" in {
      val mockUserAnswersService = mock[UserAnswersService]
      val mockSessionRepository  = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockUserAnswersService.setExternalUserAnswers(any())(any()))
        .thenReturn(Future.successful(Left(UserAnswersErrorResponse("Error", None))))

      val application = applicationBuilder(Some(userAnswersMemberNameQtNumber))
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

    "must redirect to final Check Your Answers page for a POST fromFinalCYA = true and Mode = CheckMode" in {
      val mockUserAnswersService = mock[UserAnswersService]
      val mockSessionRepository  = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockUserAnswersService.setExternalUserAnswers(any())(any()))
        .thenReturn(Future.successful(Right(Done)))

      val application = applicationBuilder(Some(userAnswersMemberNameQtNumber))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UserAnswersService].toInstance(mockUserAnswersService)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, routes.MemberDateOfBirthController.onSubmit(CheckMode, fromFinalCYA = true).url)
            .withFormUrlEncodedBody(
              "value.day"   -> validAnswer.getDayOfMonth.toString,
              "value.month" -> validAnswer.getMonthValue.toString,
              "value.year"  -> validAnswer.getYear.toString
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.checkYourAnswers.routes.CheckYourAnswersController.onPageLoad().url
      }
    }
  }
}

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
import models.TaskCategory._
import models.{TaskCategory, UserAnswers}
import models.taskList.TaskStatus
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.TaskStatusQuery
import repositories.SessionRepository
import services.UserAnswersService
import viewmodels.govuk.SummaryListFluency
import views.html.memberDetails.MemberDetailsCYAView
import models.responses.UserAnswersErrorResponse

import scala.concurrent.Future

class MemberDetailsCYAControllerSpec
    extends AnyFreeSpec
    with SpecBase
    with SummaryListFluency
    with MockitoSugar {

  "Check Your Answers Controller" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = userAnswersQtNumber).build()

      running(application) {
        val request = FakeRequest(GET, routes.MemberDetailsCYAController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MemberDetailsCYAView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list)(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must set MemberDetails to Completed on POST, persist externally, and redirect to Task List" in {
      val mockUserAnswersService = mock[UserAnswersService]
      val mockSessionRepository  = mock[SessionRepository]

      when(mockSessionRepository.set(any[UserAnswers])) thenReturn Future.successful(true)
      when(mockUserAnswersService.setExternalUserAnswers(any[UserAnswers])(any()))
        .thenReturn(Future.successful(Right(Done)))

      val application =
        applicationBuilder(userAnswers = emptyUserAnswers)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          )
          .build()

      running(application) {
        val request = FakeRequest(POST, routes.MemberDetailsCYAController.onSubmit().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.TaskListController.onPageLoad().url

        verify(mockSessionRepository).set(
          org.mockito.ArgumentMatchers.argThat[UserAnswers] { ua =>
            ua.get(TaskStatusQuery(TaskCategory.MemberDetails)).contains(TaskStatus.Completed)
          }
        )

        verify(mockUserAnswersService).setExternalUserAnswers(
          org.mockito.ArgumentMatchers.argThat[UserAnswers] { ua =>
            ua.get(TaskStatusQuery(TaskCategory.MemberDetails)).contains(TaskStatus.Completed)
          }
        )(any())
      }
    }

    "must redirect to Journey Recovery on POST when external persistence returns Left" in {
      val mockUserAnswersService = mock[UserAnswersService]
      val mockSessionRepository  = mock[SessionRepository]

      when(mockSessionRepository.set(any[UserAnswers])) thenReturn Future.successful(true)
      when(mockUserAnswersService.setExternalUserAnswers(any[UserAnswers])(any()))
        .thenReturn(Future.successful(Left(UserAnswersErrorResponse("boom", None))))

      val application =
        applicationBuilder(userAnswers = emptyUserAnswers)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          )
          .build()

      running(application) {
        val request = FakeRequest(POST, routes.MemberDetailsCYAController.onSubmit().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

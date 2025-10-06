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

import base.SpecBase
import models.TaskCategory.{MemberDetails, QROPSDetails, SchemeManagerDetails, SubmissionDetails, TransferDetails}
import models.responses.UserAnswersErrorResponse
import models.taskList.TaskStatus
import models.{SessionData, TaskCategory, UserAnswers}
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.OK
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.TaskStatusQuery
import repositories.SessionRepository
import services.UserAnswersService
import viewmodels.govuk.SummaryListFluency

import scala.concurrent.Future

class TaskListControllerSpec
    extends AnyFreeSpec
    with SpecBase
    with SummaryListFluency
    with MockitoSugar {

  "TaskListController" - {

    "must return OK and NOT persist when no task statuses change" in {
      val mockSessionRepository = mock[SessionRepository]

      val initialSD =
        sessionDataQtNumber
          .set(TaskStatusQuery(TaskCategory.MemberDetails), TaskStatus.Completed).success.value
          .set(TaskStatusQuery(TaskCategory.TransferDetails), TaskStatus.Completed).success.value
          .set(TaskStatusQuery(TaskCategory.QROPSDetails), TaskStatus.Completed).success.value
          .set(TaskStatusQuery(TaskCategory.SchemeManagerDetails), TaskStatus.Completed).success.value
          .set(TaskStatusQuery(TaskCategory.SubmissionDetails), TaskStatus.NotStarted).success.value

      val app =
        applicationBuilder(sessionData = initialSD)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(app) {
        val req = FakeRequest(GET, controllers.routes.TaskListController.onPageLoad().url)
        val res = route(app, req).value

        status(res) mustEqual OK

        verify(mockSessionRepository, never()).set(any[SessionData])
      }
    }

    "must redirect to Journey Recovery when persistence changes are needed but external save fails" in {
      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any[SessionData])) thenReturn Future.successful(false)

      val initialSD =
        sessionDataQtNumber
          .set(TaskStatusQuery(TaskCategory.MemberDetails), TaskStatus.Completed).success.value
          .set(TaskStatusQuery(TaskCategory.TransferDetails), TaskStatus.Completed).success.value
          .set(TaskStatusQuery(TaskCategory.QROPSDetails), TaskStatus.Completed).success.value
          .set(TaskStatusQuery(TaskCategory.SchemeManagerDetails), TaskStatus.Completed).success.value
          .set(TaskStatusQuery(TaskCategory.SubmissionDetails), TaskStatus.CannotStart).success.value

      val app =
        applicationBuilder(sessionData = initialSD)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(app) {
        val req = FakeRequest(GET, controllers.routes.TaskListController.onPageLoad().url)
        val res = route(app, req).value

        status(res) mustEqual SEE_OTHER
        redirectLocation(res).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockSessionRepository, times(1)).set(any)
      }
    }

    "must (re)block dependent tasks and Submission when MemberDetails is not Completed" in {
      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any[SessionData])) thenReturn Future.successful(true)

      val initialSD =
        sessionDataQtNumber
          .set(TaskStatusQuery(TaskCategory.MemberDetails), TaskStatus.NotStarted).success.value
          .set(TaskStatusQuery(TaskCategory.TransferDetails), TaskStatus.NotStarted).success.value
          .set(TaskStatusQuery(TaskCategory.QROPSDetails), TaskStatus.Completed).success.value
          .set(TaskStatusQuery(TaskCategory.SchemeManagerDetails), TaskStatus.NotStarted).success.value
          .set(TaskStatusQuery(TaskCategory.SubmissionDetails), TaskStatus.NotStarted).success.value

      val app =
        applicationBuilder(sessionData = initialSD)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(app) {
        val req = FakeRequest(GET, controllers.routes.TaskListController.onPageLoad().url)
        val res = route(app, req).value

        status(res) mustEqual OK

        verify(mockSessionRepository).set(ArgumentMatchers.argThat[SessionData] { sd =>
          sd.get(TaskStatusQuery(TaskCategory.TransferDetails)).contains(TaskStatus.CannotStart) &&
          sd.get(TaskStatusQuery(TaskCategory.QROPSDetails)).contains(TaskStatus.CannotStart) &&
          sd.get(TaskStatusQuery(TaskCategory.SchemeManagerDetails)).contains(TaskStatus.CannotStart) &&
          sd.get(TaskStatusQuery(TaskCategory.SubmissionDetails)).contains(TaskStatus.CannotStart)
        })
      }
    }

    "must unblock dependent tasks (CannotStart -> NotStarted) on load when MemberDetails is completed" in {
      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any[SessionData])) thenReturn Future.successful(true)

      val initialSD: SessionData =
        sessionDataQtNumber
          .set(TaskStatusQuery(MemberDetails), TaskStatus.Completed).success.value
          .set(TaskStatusQuery(TransferDetails), TaskStatus.CannotStart).success.value
          .set(TaskStatusQuery(QROPSDetails), TaskStatus.CannotStart).success.value
          .set(TaskStatusQuery(SchemeManagerDetails), TaskStatus.CannotStart).success.value
          .set(TaskStatusQuery(SubmissionDetails), TaskStatus.CannotStart).success.value

      val app =
        applicationBuilder(sessionData = initialSD)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(app) {
        val req = FakeRequest(GET, controllers.routes.TaskListController.onPageLoad().url)
        val res = route(app, req).value

        status(res) mustEqual OK

        verify(mockSessionRepository).set(ArgumentMatchers.argThat[SessionData] { ua =>
          ua.get(TaskStatusQuery(TransferDetails)).contains(TaskStatus.NotStarted) &&
          ua.get(TaskStatusQuery(QROPSDetails)).contains(TaskStatus.NotStarted) &&
          ua.get(TaskStatusQuery(SchemeManagerDetails)).contains(TaskStatus.NotStarted) &&
          ua.get(TaskStatusQuery(SubmissionDetails)).contains(TaskStatus.CannotStart)
        })
      }
    }

    "must set SubmissionDetails to NotStarted on load when all prerequisites are Completed" in {
      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any[SessionData])) thenReturn Future.successful(true)

      val initialSD =
        sessionDataQtNumber
          .set(TaskStatusQuery(TaskCategory.MemberDetails), TaskStatus.Completed).success.value
          .set(TaskStatusQuery(TaskCategory.TransferDetails), TaskStatus.Completed).success.value
          .set(TaskStatusQuery(TaskCategory.QROPSDetails), TaskStatus.Completed).success.value
          .set(TaskStatusQuery(TaskCategory.SchemeManagerDetails), TaskStatus.Completed).success.value
          .set(TaskStatusQuery(TaskCategory.SubmissionDetails), TaskStatus.CannotStart).success.value

      val app =
        applicationBuilder(sessionData = initialSD)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(app) {
        val req = FakeRequest(GET, controllers.routes.TaskListController.onPageLoad().url)
        val res = route(app, req).value

        status(res) mustEqual OK

        verify(mockSessionRepository).set(ArgumentMatchers.argThat[SessionData] { ua =>
          ua.get(TaskStatusQuery(TaskCategory.SubmissionDetails)).contains(TaskStatus.NotStarted)
        })
      }
    }
  }
}

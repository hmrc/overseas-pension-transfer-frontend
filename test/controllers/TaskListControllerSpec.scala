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
import connectors.TransferConnector
import models.TaskCategory.{MemberDetails, QROPSDetails, SchemeManagerDetails, SubmissionDetails, TransferDetails}
import models.responses.UserAnswersErrorResponse
import models.taskList.TaskStatus
import models.{PstrNumber, QtNumber, QtStatus, TaskCategory, UserAnswers}
import models.dtos.UserAnswersDTO
import models.{SessionData, TaskCategory, UserAnswers}
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.{any, eq => eqTo, isNull}
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
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.govuk.SummaryListFluency

import scala.concurrent.{ExecutionContext, Future}

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

    "continueJourney must fetch specific transfer, save UA, and redirect to onPageLoad on success" in {
      val mockSessionRepository = mock[SessionRepository]
      val mockTransferConnector = mock[TransferConnector]

      when(mockSessionRepository.set(any[SessionData])) thenReturn Future.successful(true)

      val dto = UserAnswersDTO(
        referenceId = "REF123",
        pstr        = pstr,
        data        = emptyUserAnswers.data,
        lastUpdated = emptyUserAnswers.lastUpdated
      )

      when(
        mockTransferConnector.getSpecificTransfer(
          transferReference = any[Option[String]],
          qtNumber          = any[Option[QtNumber]],
          pstrNumber        = any[PstrNumber],
          qtStatus          = any[QtStatus],
          versionNumber     = any[Option[String]]
        )(any[HeaderCarrier], any[ExecutionContext])
      ).thenReturn(Future.successful(Right(dto)))

      val app =
        applicationBuilder()
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[TransferConnector].toInstance(mockTransferConnector)
          )
          .build()

      running(app) {
        val req = FakeRequest(
          GET,
          routes.TaskListController
            .continueJourney(
              referenceId   = "REF123",
              pstr          = PstrNumber(pstr.value),
              qtStatus      = QtStatus.InProgress,
              versionNumber = None
            )
            .url
        )
        val res = route(app, req).value

        status(res) mustBe SEE_OTHER
        redirectLocation(res).value mustBe routes.TaskListController.onPageLoad().url

        verify(mockSessionRepository).set(any[SessionData])

        verify(mockTransferConnector).getSpecificTransfer(
          transferReference = eqTo(Some("REF123")),
          qtNumber          = isNull[Option[QtNumber]](),
          pstrNumber        = eqTo(PstrNumber(pstr.value)),
          qtStatus          = eqTo(QtStatus.InProgress),
          versionNumber     = isNull[Option[String]]()
        )(any[HeaderCarrier], any[ExecutionContext])
      }
    }

    "continueJourney must redirect to JourneyRecovery when connector returns an error" in {
      val mockTransferConnector = mock[TransferConnector]

      when(
        mockTransferConnector.getSpecificTransfer(
          transferReference = any[Option[String]],
          qtNumber          = any[Option[QtNumber]],
          pstrNumber        = any[PstrNumber],
          qtStatus          = any[QtStatus],
          versionNumber     = any[Option[String]]
        )(any[HeaderCarrier], any[ExecutionContext])
      ).thenReturn(Future.successful(Left(UserAnswersErrorResponse("not found", None))))

      val app =
        applicationBuilder()
          .overrides(bind[TransferConnector].toInstance(mockTransferConnector))
          .build()

      running(app) {
        val req = FakeRequest(
          GET,
          routes.TaskListController
            .continueJourney("REF123", PstrNumber(pstr.value), QtStatus.Submitted, None)
            .url
        )
        val res = route(app, req).value

        status(res) mustBe SEE_OTHER
        redirectLocation(res).value mustBe routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

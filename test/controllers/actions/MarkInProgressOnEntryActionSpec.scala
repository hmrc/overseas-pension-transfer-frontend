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

package controllers.actions

import base.SpecBase
import models.requests.{DataRequest, DisplayRequest}
import models.responses.UserAnswersErrorResponse
import models.taskList.TaskStatus
import models.{CheckMode, NormalMode, QtNumber, TaskCategory, UserAnswers}
import org.apache.pekko.Done
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Results._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.TaskStatusQuery
import repositories.SessionRepository
import services.UserAnswersService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MarkInProgressOnEntryActionSpec
    extends AnyFreeSpec
    with SpecBase
    with MockitoSugar {

  private def mkDisplayRequest[A](ua: UserAnswers) =
    DisplayRequest(
      request               = FakeRequest(),
      authenticatedUser     = psaUser,
      userAnswers           = ua,
      memberName            = "Firstname Lastname",
      qtNumber              = QtNumber.empty,
      dateTransferSubmitted = "Transfer not submitted"
    )

  "MarkInProgressOnEntryActionImpl.forCategoryAndMode" - {

    "in NormalMode must set the task to InProgress, persist to session + external, " +
      "and pass the updated DataRequest to the downstream request" in {

        val sessionRepo = mock[SessionRepository]
        val uaService   = mock[UserAnswersService]

        when(sessionRepo.set(any[UserAnswers])) thenReturn Future.successful(true)
        when(uaService.setExternalUserAnswers(any[UserAnswers])(any()))
          .thenReturn(Future.successful(Right(Done)))

        val action  = new MarkInProgressOnEntryActionImpl(sessionRepo, uaService)
        val refiner = action.forCategoryAndMode(TaskCategory.MemberDetails, NormalMode)

        val req = mkDisplayRequest(emptyUserAnswers)

        val resultFuture =
          refiner.invokeBlock(
            request = req,
            block   = (_: DisplayRequest[_]) => Future.successful(Ok)
          )

        status(resultFuture) mustBe OK

        val uaCaptor1 = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(sessionRepo).set(uaCaptor1.capture())

        val persistedUa = uaCaptor1.getValue
        persistedUa.get(TaskStatusQuery(TaskCategory.MemberDetails)) mustBe Some(TaskStatus.InProgress)

        val uaCaptor2 = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(uaService).setExternalUserAnswers(uaCaptor2.capture())(any())

        val externalUa = uaCaptor2.getValue
        externalUa.get(TaskStatusQuery(TaskCategory.MemberDetails)) mustBe Some(TaskStatus.InProgress)
      }

    "in NormalMode must short-circuit with Redirect when external persistence returns Left" in {

      val sessionRepo = mock[SessionRepository]
      val uaService   = mock[UserAnswersService]

      when(sessionRepo.set(any[UserAnswers])) thenReturn Future.successful(true)
      when(uaService.setExternalUserAnswers(any[UserAnswers])(any()))
        .thenReturn(Future.successful(Left(UserAnswersErrorResponse("boom", None))))

      val action  = new MarkInProgressOnEntryActionImpl(sessionRepo, uaService)
      val refiner = action.forCategoryAndMode(TaskCategory.MemberDetails, NormalMode)

      val req = mkDisplayRequest(emptyUserAnswers)

      val resultFuture =
        refiner.invokeBlock(req, (_: DisplayRequest[_]) => Future.successful(Ok))

      status(resultFuture) mustBe SEE_OTHER
      redirectLocation(resultFuture).value mustBe controllers.routes.JourneyRecoveryController.onPageLoad().url
    }

    "in CheckMode must not persist and must pass the original DataRequest to the downstream block" in {

      val sessionRepo = mock[SessionRepository]
      val uaService   = mock[UserAnswersService]

      val action  = new MarkInProgressOnEntryActionImpl(sessionRepo, uaService)
      val refiner = action.forCategoryAndMode(TaskCategory.MemberDetails, CheckMode)

      val req = mkDisplayRequest(emptyUserAnswers)

      val resultFuture =
        refiner.invokeBlock(
          request = req,
          block   = { dr: DisplayRequest[_] =>
            dr.userAnswers mustBe req.userAnswers
            Future.successful(Ok)
          }
        )

      status(resultFuture) mustBe OK

      verify(sessionRepo, never()).set(any[UserAnswers])
      verify(uaService, never()).setExternalUserAnswers(any[UserAnswers])(any())
    }

  }
}

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

import models.requests.DisplayRequest
import models.taskList.TaskStatus.InProgress
import models.{Mode, NormalMode, TaskCategory}
import play.api.mvc.Results.Redirect
import play.api.mvc._
import queries.TaskStatusQuery
import repositories.SessionRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait MarkInProgressOnEntryAction {
  def forCategoryAndMode(category: TaskCategory, mode: Mode): ActionRefiner[DisplayRequest, DisplayRequest]
}

class MarkInProgressOnEntryActionImpl @Inject() (
    sessionRepository: SessionRepository
  )(implicit ec: ExecutionContext
  ) extends MarkInProgressOnEntryAction {

  /** ActionRefiner that marks the given task category as InProgress when entering a journey in NormalMode. Persists the updated UserAnswers to session and
    * middleware. Falls back to JourneyRecovery if persistence fails.
    */
  override def forCategoryAndMode(category: TaskCategory, mode: Mode): ActionRefiner[DisplayRequest, DisplayRequest] =
    new ActionRefiner[DisplayRequest, DisplayRequest] {
      override protected def executionContext: ExecutionContext = ec

      override protected def refine[A](request: DisplayRequest[A]): Future[Either[Result, DisplayRequest[A]]] = {
        mode match {
          case NormalMode =>
            for {
              updated <- Future.fromTry(request.sessionData.set(TaskStatusQuery(category), InProgress))
              saved   <- sessionRepository.set(updated)
            } yield {
              if (saved) {
                Right(request.copy(sessionData = updated))
              } else {
                Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
              }
            }

          case _ => Future.successful(Right(request))
        }
      }
    }
}

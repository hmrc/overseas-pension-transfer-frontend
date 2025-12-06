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

package services

import models.TaskCategory.SubmissionDetails
import models.taskList.TaskStatus.{CannotStart, Completed, InProgress, NotStarted}
import models.{CheckMode, Mode, SessionData, TaskCategory, UserAnswers}
import queries.TaskStatusQuery

import scala.util.{Success, Try}

object TaskService {

  def updateTaskStatusesOnMemberDetailsComplete(sessionData: SessionData): Try[SessionData] = {
    val others =
      TaskCategory.valuesWithoutSubmission.filterNot(_ == TaskCategory.MemberDetails)

    sessionData.get(TaskStatusQuery(TaskCategory.MemberDetails)) match {
      case Some(Completed) =>
        // Unblock: only lift those that are currently CannotStart -> NotStarted
        others.foldLeft(Try(sessionData)) { (tasks, cat) =>
          tasks.flatMap { sd =>
            if (sd.get(TaskStatusQuery(cat)).contains(CannotStart)) {
              sd.set(TaskStatusQuery(cat), NotStarted)
            } else {
              Success(sd)
            }
          }
        }

      case _ =>
        // Block: force others to CannotStart if MemberDetails in progress
        others.foldLeft(Try(sessionData)) { (tasks, cat) =>
          tasks.flatMap(_.set(TaskStatusQuery(cat), CannotStart))
        }
    }
  }

  def updateSubmissionTaskStatus(sessionData: SessionData): Try[SessionData] = {
    val allPrereqsDone =
      TaskCategory.valuesWithoutSubmission.forall { cat =>
        sessionData.get(TaskStatusQuery(cat)).contains(Completed)
      }

    if (allPrereqsDone) {
      sessionData.get(TaskStatusQuery(SubmissionDetails)) match {
        case Some(CannotStart) | None =>
          sessionData.set(TaskStatusQuery(SubmissionDetails), NotStarted)
        case _                        =>
          Success(sessionData)
      }
    } else {
      // At least one prerequisite isn’t Completed → (re)block submission
      sessionData.set(TaskStatusQuery(SubmissionDetails), CannotStart)
    }
  }

  def setInProgressInCheckMode(mode: Mode, userAnswers: UserAnswers, taskCategory: TaskCategory): Try[UserAnswers] =
    mode match {
      case CheckMode =>
        userAnswers.set(TaskStatusQuery(taskCategory), InProgress)
      case _         =>
        Success(userAnswers)
    }
}

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

import models.TaskCategory.{MemberDetails, SubmissionDetails}
import models.taskList.TaskStatus.{CannotStart, Completed, InProgress, NotStarted}
import models.{CheckMode, Mode, TaskCategory, UserAnswers}
import queries.TaskStatusQuery

import scala.util.{Success, Try}

object TaskService {

  def updateTaskStatusesOnMemberDetailsComplete(userAnswers: UserAnswers): Try[UserAnswers] = {
    val others =
      TaskCategory.valuesWithoutSubmission.filterNot(_ == TaskCategory.MemberDetails)

    userAnswers.get(TaskStatusQuery(TaskCategory.MemberDetails)) match {
      case Some(Completed) =>
        // Unblock: only lift those that are currently CannotStart -> NotStarted
        others.foldLeft(Try(userAnswers)) { (uaT, cat) =>
          uaT.flatMap { ua =>
            if (ua.get(TaskStatusQuery(cat)).contains(CannotStart)) {
              ua.set(TaskStatusQuery(cat), NotStarted)
            } else {
              Success(ua)
            }
          }
        }

      case _ =>
        // Block: force others to CannotStart if MemberDetails in progress
        others.foldLeft(Try(userAnswers)) { (uaT, cat) =>
          uaT.flatMap(_.set(TaskStatusQuery(cat), CannotStart))
        }
    }
  }

  def updateSubmissionTaskStatus(userAnswers: UserAnswers): Try[UserAnswers] = {
    val allPrereqsDone =
      TaskCategory.valuesWithoutSubmission.forall { cat =>
        userAnswers.get(TaskStatusQuery(cat)).contains(Completed)
      }

    if (allPrereqsDone) {
      userAnswers.get(TaskStatusQuery(SubmissionDetails)) match {
        case Some(CannotStart) | None =>
          userAnswers.set(TaskStatusQuery(SubmissionDetails), NotStarted)
        case _                        =>
          Success(userAnswers)
      }
    } else {
      // At least one prerequisite isn’t Completed → (re)block submission
      userAnswers.set(TaskStatusQuery(SubmissionDetails), CannotStart)
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

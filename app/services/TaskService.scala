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

import com.google.inject.{Inject, Singleton}
import models.TaskCategory.{MemberDetails, SubmissionDetails}
import models.taskList.TaskStatus.{CannotStart, Completed, InProgress, NotStarted}
import models.{CheckMode, Mode, TaskCategory, UserAnswers}
import queries.TaskStatusQuery

import scala.util.{Success, Try}

@Singleton
class TaskService @Inject() {

  def unblockTasksOnMemberDetailsCompletion(userAnswers: UserAnswers): Try[UserAnswers] = {
    userAnswers
      .get(TaskStatusQuery(TaskCategory.MemberDetails))
      .filter(_ == Completed)
      .map { _ =>
        TaskCategory.valuesWithoutSubmission.foldLeft(Try(userAnswers)) { (uaTry, category) =>
          uaTry.flatMap(ua =>
            if (ua.get(TaskStatusQuery(category)).contains(CannotStart)) {
              ua.set(TaskStatusQuery(category), NotStarted)
            } else {
              Try(ua)
            }
          )
        }
      }
      .getOrElse(Try(userAnswers))
  }

  def unblockSubmissionOnAllTasksCompletion(userAnswers: UserAnswers): Try[UserAnswers] = {
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
      Success(userAnswers)
    }
  }

  def setInProgressInCheckMode(mode: Mode, userAnswers: UserAnswers): Try[UserAnswers] =
    mode match {
      case CheckMode =>
        userAnswers.set(TaskStatusQuery(MemberDetails), InProgress)
      case _         =>
        Success(userAnswers)
    }
}

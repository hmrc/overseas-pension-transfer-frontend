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

import base.SpecBase
import models.taskList.TaskStatus.{CannotStart, Completed, InProgress, NotStarted}
import models.{TaskCategory, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import queries.TaskStatusQuery

class TaskServiceSpec extends AnyFreeSpec with SpecBase with Matchers {

  private val service = new TaskService

  "unblockTasksOnMemberDetailsCompletion" - {

    "must set non-submission tasks from CannotStart -> NotStarted when MemberDetails is Completed " +
      "and leave other statuses and SubmissionDetails unchanged" in {
        val ua0: UserAnswers =
          emptyUserAnswers
            .set(TaskStatusQuery(TaskCategory.MemberDetails), Completed).success.value
            .set(TaskStatusQuery(TaskCategory.TransferDetails), CannotStart).success.value
            .set(TaskStatusQuery(TaskCategory.QROPSDetails), InProgress).success.value
            .set(TaskStatusQuery(TaskCategory.SchemeManagerDetails), CannotStart).success.value
            .set(TaskStatusQuery(TaskCategory.SubmissionDetails), CannotStart).success.value
        val result           = service.unblockTasksOnMemberDetailsCompletion(ua0)

        result.isSuccess mustBe true
        val ua = result.get

        ua.get(TaskStatusQuery(TaskCategory.MemberDetails)) mustBe Some(Completed)
        ua.get(TaskStatusQuery(TaskCategory.TransferDetails)) mustBe Some(NotStarted)
        ua.get(TaskStatusQuery(TaskCategory.SchemeManagerDetails)) mustBe Some(NotStarted)
        ua.get(TaskStatusQuery(TaskCategory.QROPSDetails)) mustBe Some(InProgress)
        ua.get(TaskStatusQuery(TaskCategory.SubmissionDetails)) mustBe Some(CannotStart)
      }

    "must leave UserAnswers unchanged when MemberDetails is not Completed" in {
      val ua0: UserAnswers =
        emptyUserAnswers
          .set(TaskStatusQuery(TaskCategory.MemberDetails), NotStarted).success.value
          .set(TaskStatusQuery(TaskCategory.TransferDetails), CannotStart).success.value
          .set(TaskStatusQuery(TaskCategory.SubmissionDetails), CannotStart).success.value

      val result = service.unblockTasksOnMemberDetailsCompletion(ua0)

      result.isSuccess mustBe true
      val ua = result.get

      ua.get(TaskStatusQuery(TaskCategory.MemberDetails)) mustBe Some(NotStarted)
      ua.get(TaskStatusQuery(TaskCategory.TransferDetails)) mustBe Some(CannotStart)
      ua.get(TaskStatusQuery(TaskCategory.SubmissionDetails)) mustBe Some(CannotStart)
    }
  }
}

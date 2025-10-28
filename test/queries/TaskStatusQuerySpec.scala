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

package queries

import base.SpecBase
import models.{PstrNumber, TaskCategory, UserAnswers}
import models.taskList.TaskStatus
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class TaskStatusQuerySpec extends AnyFreeSpec with Matchers with SpecBase {

  "TaskStatusQuery" - {
    "should write and read at category.status" in {
      val ua0 = UserAnswers(userAnswersTransferNumber, PstrNumber("12345678AB"))
      val q   = TaskStatusQuery(TaskCategory.MemberDetails)

      val ua1 = ua0.set(q, TaskStatus.InProgress).get

      (ua1.data \ "memberDetails" \ "status").as[String] mustBe "inProgress"
      ua1.get(q) mustBe Some(TaskStatus.InProgress)
    }

    "setting one category should not affect another" in {
      val ua0 = UserAnswers(userAnswersTransferNumber, PstrNumber("12345678AB"))
      val mQ  = TaskStatusQuery(TaskCategory.MemberDetails)
      val tQ  = TaskStatusQuery(TaskCategory.TransferDetails)

      val ua1 = ua0.set(mQ, TaskStatus.NotStarted).get

      ua1.get(mQ) mustBe Some(TaskStatus.NotStarted)
      ua1.get(tQ) mustBe None
    }

    "remove should prune the status key and be idempotent" in {
      val q   = TaskStatusQuery(TaskCategory.MemberDetails)
      val ua1 = UserAnswers(userAnswersTransferNumber, PstrNumber("12345678AB")).set(q, TaskStatus.InProgress).get
      val ua2 = ua1.remove(q).get
      val ua3 = ua2.remove(q).get

      (ua2.data \ "memberDetails" \ "status").toOption mustBe None
      ua3.data mustBe ua2.data
    }
  }
}

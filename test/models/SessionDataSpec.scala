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

package models

import base.SpecBase
import models.TaskCategory._
import models.taskList.TaskStatus
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import queries.TaskStatusQuery

class SessionDataSpec extends AnyFreeSpec with Matchers with SpecBase {

  "initialise" - {
    "should set expected default statuses" in {
      val sd = SessionData.initialise(emptySessionData).get

      sd.transferId mustBe "id"
      sd.get(TaskStatusQuery(MemberDetails)) mustBe Some(TaskStatus.NotStarted)
      sd.get(TaskStatusQuery(QROPSDetails)) mustBe Some(TaskStatus.CannotStart)
      sd.get(TaskStatusQuery(SchemeManagerDetails)) mustBe Some(TaskStatus.CannotStart)
      sd.get(TaskStatusQuery(TransferDetails)) mustBe Some(TaskStatus.CannotStart)
      sd.get(TaskStatusQuery(SubmissionDetails)) mustBe Some(TaskStatus.CannotStart)
    }
  }

}

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
import pages.SubmitToHMRCPage
import play.api.libs.json.Json
import queries.TaskStatusQuery

class SessionDataSpec extends AnyFreeSpec with Matchers with SpecBase {

  "get" - {
    "should get Some value from data Json when value is present" in {
      emptySessionData.copy(data = Json.obj("submitToHMRC" -> true, "key" -> "value")).get(SubmitToHMRCPage) mustBe
        Some(true)
    }

    "should get None when no value present in data Json" in {
      emptySessionData.get(SubmitToHMRCPage) mustBe None
    }
  }

  "set" - {
    "should update Json in data field" in {
      emptySessionData.set(SubmitToHMRCPage, false).success.value.data mustBe
        Json.obj("submitToHMRC" -> false)
    }
  }

  "remove" - {
    "should remove existing Json from data field" in {
      emptySessionData.copy(data = Json.obj("submitToHMRC" -> true, "key" -> "value"))
        .remove(SubmitToHMRCPage).success.value.data mustBe
        Json.obj("key" -> "value")
    }
  }

  "initialise" - {
    "should set expected default statuses" in {
      val sd = SessionData.initialise(emptySessionData).get

      sd.transferId mustBe userAnswersTransferNumber
      sd.get(TaskStatusQuery(MemberDetails)) mustBe Some(TaskStatus.NotStarted)
      sd.get(TaskStatusQuery(QROPSDetails)) mustBe Some(TaskStatus.CannotStart)
      sd.get(TaskStatusQuery(SchemeManagerDetails)) mustBe Some(TaskStatus.CannotStart)
      sd.get(TaskStatusQuery(TransferDetails)) mustBe Some(TaskStatus.CannotStart)
      sd.get(TaskStatusQuery(SubmissionDetails)) mustBe Some(TaskStatus.CannotStart)
    }
  }

}

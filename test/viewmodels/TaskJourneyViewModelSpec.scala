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

package viewmodels

import base.SpecBase
import models.taskList.TaskStatus
import models.{Mode, NormalMode, TaskCategory}
import models.taskList.TaskStatus.{Completed, InProgress, NotStarted}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import play.api.libs.json.Json
import queries.TaskStatusQuery
import org.scalatest.prop.TableDrivenPropertyChecks._
import viewmodels.TaskJourneyViewModels.MemberDetailsJourneyViewModel

import java.time.LocalDate

class TaskJourneyViewModelSpec extends AnyFreeSpec with Matchers with SpecBase {

  "MemberDetailsJourneyViewModel" - {
    "status" - {
      "must return Completed TaskStatus when Valid MemberDetails returned from Validator" in {
        val validMemberDetailsJson = Json.obj("memberDetails" -> Json.obj(
          "name"                   -> Json.obj("firstName" -> "Firstname", "lastName" -> "Lastname"),
          "nino"                   -> "AA000000A",
          "dateOfBirth"            -> LocalDate.of(1993, 11, 11),
          "principalResAddDetails" -> Json.obj(
            "addressLine1" -> "line1",
            "addressLine2" -> "line2",
            "country"      -> Json.obj("code" -> "GB", "name" -> "United Kingdom")
          ),
          "memUkResident"          -> true
        ))

        MemberDetailsJourneyViewModel.status(emptyUserAnswers.copy(data = validMemberDetailsJson)) mustBe
          Completed
      }

      "must return NotStarted when an empty UserAnswers" in {
        MemberDetailsJourneyViewModel.status(emptyUserAnswers) mustBe
          NotStarted
      }

      "must return InProgress when other Invalid Chain is returned" in {
        val missingMemberDetailsJson = Json.obj("memberDetails" -> Json.obj(
          "name" -> Json.obj("firstName" -> "Firstname", "lastName" -> "Lastname")
        ))

        MemberDetailsJourneyViewModel.status(emptyUserAnswers.copy(data = missingMemberDetailsJson)) mustBe
          InProgress
      }
    }

    "entry" - {
      "route to CYA when status is Completed" in {
        val validMemberDetailsJson = Json.obj("memberDetails" -> Json.obj(
          "name"                   -> Json.obj("firstName" -> "Firstname", "lastName" -> "Lastname"),
          "nino"                   -> "AA000000A",
          "dateOfBirth"            -> LocalDate.of(1993, 11, 11),
          "principalResAddDetails" -> Json.obj(
            "addressLine1" -> "line1",
            "addressLine2" -> "line2",
            "country"      -> Json.obj("code" -> "GB", "name" -> "United Kingdom")
          ),
          "memUkResident"          -> true
        ))

        MemberDetailsJourneyViewModel.entry(emptyUserAnswers.copy(data = validMemberDetailsJson)) mustBe
          controllers.memberDetails.routes.MemberDetailsCYAController.onPageLoad()
      }

      "route to member name page when status is not started" in {
        val validMemberDetailsJson = Json.obj("memberDetails" -> Json.obj())

        MemberDetailsJourneyViewModel.entry(emptyUserAnswers.copy(data = validMemberDetailsJson)) mustBe
          controllers.memberDetails.routes.MemberNameController.onPageLoad(NormalMode)
      }
    }
  }
}

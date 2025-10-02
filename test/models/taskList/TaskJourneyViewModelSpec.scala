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

package models.taskList

import base.SpecBase
import models.{Mode, NormalMode, TaskCategory, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks._
import queries.TaskStatusQuery
import viewmodels.TaskJourneyViewModels

class TaskJourneyViewModelSpec extends AnyFreeSpec with SpecBase with Matchers {

  private val journeys =
    Table(
      ("name", "journey", "category", "expectedStart", "expectedCya"),
      (
        "MemberDetails",
        TaskJourneyViewModels.MemberDetailsJourneyViewModel,
        TaskCategory.MemberDetails,
        (m: Mode) => controllers.memberDetails.routes.MemberNameController.onPageLoad(m),
        () => controllers.memberDetails.routes.MemberDetailsCYAController.onPageLoad()
      ),
      (
        "TransferDetails",
        TaskJourneyViewModels.TransferDetailsJourneyViewModel,
        TaskCategory.TransferDetails,
        (m: Mode) => controllers.transferDetails.routes.OverseasTransferAllowanceController.onPageLoad(m),
        () => controllers.transferDetails.routes.TransferDetailsCYAController.onPageLoad()
      ),
      (
        "QROPSDetails",
        TaskJourneyViewModels.QropsDetailsJourneyViewModel,
        TaskCategory.QROPSDetails,
        (m: Mode) => controllers.qropsDetails.routes.QROPSNameController.onPageLoad(m),
        () => controllers.qropsDetails.routes.QROPSDetailsCYAController.onPageLoad()
      ),
      (
        "SchemeManagerDetails",
        TaskJourneyViewModels.SchemeManagerDetailsJourneyViewModel,
        TaskCategory.SchemeManagerDetails,
        (m: Mode) => controllers.qropsSchemeManagerDetails.routes.SchemeManagerTypeController.onPageLoad(m),
        () => controllers.qropsSchemeManagerDetails.routes.SchemeManagerDetailsCYAController.onPageLoad()
      ),
      (
        "SubmissionDetails",
        TaskJourneyViewModels.SubmissionDetailsJourneyViewModel,
        TaskCategory.SubmissionDetails,
        (_: Mode) => controllers.checkYourAnswers.routes.CheckYourAnswersController.onPageLoad(),
        () => controllers.checkYourAnswers.routes.CheckYourAnswersController.onPageLoad()
      )
    )

  "entry(ua)" - {

    "routes to CYA when status is Completed" in {
      forAll(journeys) { (_, journey, category, _, expectedCya) =>
        val ua   = UserAnswers(userAnswersId, pstr)
          .set(TaskStatusQuery(category), TaskStatus.Completed).success.value
        val call = journey.entry(ua)

        call.url mustEqual expectedCya().url
      }
    }

    "routes to start (NormalMode) for NotStarted / InProgress / CannotStart" in {
      val nonCompleted = Table("status", TaskStatus.NotStarted, TaskStatus.InProgress, TaskStatus.CannotStart)

      forAll(journeys) { (_, journey, category, expectedStart, _) =>
        forAll(nonCompleted) { s =>
          val ua   = UserAnswers(userAnswersId, pstr).set(TaskStatusQuery(category), s).success.value
          val call = journey.entry(ua)

          call.url mustEqual expectedStart(NormalMode).url
        }
      }
    }
  }

  "status(ua)" - {
    "returns CannotStart by default for every journey when not set" in {
      val ua = UserAnswers(userAnswersId, pstr)
      forAll(journeys) { (_, journey, _, _, _) =>
        journey.status(ua) mustBe TaskStatus.CannotStart
      }
    }
  }
}

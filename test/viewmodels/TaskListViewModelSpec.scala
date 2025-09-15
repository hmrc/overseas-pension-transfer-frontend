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
import models.{TaskCategory, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.i18n.Messages
import play.api.test.Helpers._
import queries.TaskStatusQuery
import uk.gov.hmrc.govukfrontend.views.Aliases.TaskListItem

class TaskListViewModelSpec extends AnyFreeSpec with SpecBase with Matchers {

  implicit val messages: Messages = stubMessagesApi().preferred(Seq.empty)

  private def findRowById(rows: Seq[TaskListItem], id: String): TaskListItem =
    rows.find(_.status.tag.exists(_.attributes.get("id").contains(s"$id-status"))).getOrElse {
      fail(s"Row with id '$id' not found; got ids: " + rows.flatMap(_.status.tag.flatMap(_.attributes.get("id"))))
    }

  "TaskListViewModel.rows" - {

    "suppresses href when status is CannotStart and shows grey tag" in {
      val application = applicationBuilder(userAnswers = emptyUserAnswers).build()
      running(application) {

        val rows = TaskListViewModel.rows(emptyUserAnswers)

        rows must not be empty
        all(rows.map(_.href)) mustBe None
        all(rows.map(_.status.tag.value.classes.contains("govuk-tag--grey"))) mustBe true
        all(rows.map(_.status.tag.value.attributes("id"))) must endWith("-status")
      }
    }

    "uses CYA link when a task is Completed (exact URL match) and shows completed tag" in {
      val application = applicationBuilder().build()
      running(application) {

        val ua = UserAnswers(userAnswersId)
          .set(TaskStatusQuery(TaskCategory.MemberDetails), TaskStatus.Completed).success.value

        val rows      = TaskListViewModel.rows(ua)
        val memberRow = findRowById(rows, TaskJourneyViewModels.MemberDetailsJourneyViewModel.id)

        memberRow.href.value mustEqual controllers.memberDetails.routes.MemberDetailsCYAController.onPageLoad().url
        memberRow.status.tag.value.classes must not include ("govuk-tag--grey")
        memberRow.status.tag.value.classes must not include ("govuk-tag--blue")
      }
    }

    "uses start(NormalMode) link when NotStarted and shows blue tag" in {
      val application = applicationBuilder().build()
      running(application) {

        val ua = UserAnswers(userAnswersId)
          .set(TaskStatusQuery(TaskCategory.MemberDetails), TaskStatus.NotStarted).success.value

        val rows      = TaskListViewModel.rows(ua)
        val memberRow = findRowById(rows, TaskJourneyViewModels.MemberDetailsJourneyViewModel.id)

        memberRow.href.value mustEqual controllers.memberDetails.routes.MemberNameController.onPageLoad(models.NormalMode).url
        memberRow.status.tag.value.classes must include("govuk-tag--blue")
      }
    }

    "uses start(NormalMode) link when InProgress and shows blue tag" in {
      val application = applicationBuilder().build()
      running(application) {

        val ua = UserAnswers(userAnswersId)
          .set(TaskStatusQuery(TaskCategory.TransferDetails), TaskStatus.InProgress).success.value

        val rows     = TaskListViewModel.rows(ua)
        val transfer = findRowById(rows, TaskJourneyViewModels.TransferDetailsJourneyViewModel.id)

        transfer.href.value mustEqual controllers.transferDetails.routes.OverseasTransferAllowanceController.onPageLoad(models.NormalMode).url
        transfer.status.tag.value.classes must include("govuk-tag--blue")
      }
    }

    "preserves the order defined by TaskJourneyViewModels.valuesWithoutSubmissionJourney" in {
      val application = applicationBuilder().build()
      running(application) {

        val rows        = TaskListViewModel.rows(emptyUserAnswers)
        val renderedIds = rows.map(_.status.tag.value.attributes("id").stripSuffix("-status"))
        val expectedIds = TaskJourneyViewModels.valuesWithoutSubmissionJourney.map(_.id)

        renderedIds mustEqual expectedIds
      }
    }
  }
}

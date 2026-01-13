/*
 * Copyright 2026 HM Revenue & Customs
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

package views

import uk.gov.hmrc.govukfrontend.views.Aliases.{TaskListItem, TaskListItemTitle}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.tasklist.TaskListItemStatus
import views.html.TaskListView
import views.utils.ViewBaseSpec

class TaskListViewSpec extends ViewBaseSpec {

  private val view = applicationBuilder().injector().instanceOf[TaskListView]

  private val taskItems = Seq(
    TaskListItem(
      title  = TaskListItemTitle(content = Text(messages("taskList.memberDetails.linkText"))),
      status = TaskListItemStatus(content = Text(messages("taskList.taskStatus.completed")))
    ),
    TaskListItem(
      title  = TaskListItemTitle(content = Text(messages("taskList.transferDetails.linkText"))),
      status = TaskListItemStatus(content = Text(messages("taskList.taskStatus.inProgress")))
    )
  )

  private val submissionItem = TaskListItem(
    title  = TaskListItemTitle(content = Text(messages("taskList.submit.linkText"))),
    status = TaskListItemStatus(content = Text(messages("taskList.taskStatus.cannotStart")))
  )

  "TaskListView" - {

    "show correct title" in {
      doc(view(taskItems, submissionItem).body).getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("taskList.title")}"
    }

    behave like pageWithH1(view(taskItems, submissionItem), "taskList.heading")

    behave like pageWithHeadings(
      view(taskItems, submissionItem),
      "h2",
      "taskList.transferDetails.heading",
      "taskList.checkAndSubmit.heading"
    )

    "display task list items" in {
      val taskLists = doc(view(taskItems, submissionItem).body).getElementsByClass("govuk-task-list")
      taskLists.size() mustBe 2
    }
  }
}

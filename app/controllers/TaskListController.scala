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

package controllers

import javax.inject.Inject
import play.api.mvc._
import play.api.i18n.I18nSupport
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import models.{Task, TaskStatus}
import uk.gov.hmrc.govukfrontend.views.Aliases.TaskListItem
import viewmodels.TaskListViewModel
import views.html.TaskListView

class TaskListController @Inject() (
    val controllerComponents: MessagesControllerComponents,
    view: TaskListView
  ) extends FrontendBaseController with I18nSupport {

  def showTaskList(): Action[AnyContent] = Action { implicit request =>
    val tasks: Seq[Task] = Seq(
      Task(
        "member-details",
        "The member details",
        "Tell us about the member you made the transfer for",
        routes.IndexController.onPageLoad(),
        TaskStatus.NotStarted
      ),
      Task(
        "transfer-details",
        "The transfer details",
        "Tell us more information about the overseas transfer",
        routes.IndexController.onPageLoad(),
        TaskStatus.CannotStart
      ),
      Task(
        "qrops-details",
        "The QROPS details",
        "Tell us more information about the QROPS",
        routes.IndexController.onPageLoad(),
        TaskStatus.CannotStart
      ),
      Task(
        "scheme-manager-details",
        "The QROPS scheme manager details",
        "Tell us more about the QROPS scheme manager",
        routes.IndexController.onPageLoad(),
        TaskStatus.CannotStart
      ),
      Task("submit", "Submit your form", "Check all your answers and submit the form", routes.IndexController.onPageLoad(), TaskStatus.CannotStart)
    )

    val taskListItems: Seq[TaskListItem] = tasks.map(TaskListViewModel.toTaskListItem)

    Ok(view(taskListItems))
  }
}

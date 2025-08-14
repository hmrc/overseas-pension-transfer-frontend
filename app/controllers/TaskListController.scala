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

import models.TaskViewModel
import models.taskList.TaskStatus
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.govukfrontend.views.Aliases.TaskListItem
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.TaskListViewModel
import views.html.TaskListView

import javax.inject.Inject

class TaskListController @Inject() (
    val controllerComponents: MessagesControllerComponents,
    view: TaskListView
  ) extends FrontendBaseController with I18nSupport {

  def showTaskList(): Action[AnyContent] = Action { implicit request =>
    val tasks: Seq[TaskViewModel] = Seq(
      TaskViewModel(
        "member-details",
        "The member details",
        "Tell us about the member you made the transfer for",
        routes.IndexController.onPageLoad(),
        TaskStatus.NotStarted
      ),
      TaskViewModel(
        "transfer-details",
        "The transfer details",
        "Tell us more information about the overseas transfer",
        routes.IndexController.onPageLoad(),
        TaskStatus.CannotStart
      ),
      TaskViewModel(
        "qrops-details",
        "The QROPS details",
        "Tell us more information about the QROPS",
        routes.IndexController.onPageLoad(),
        TaskStatus.CannotStart
      ),
      TaskViewModel(
        "scheme-manager-details",
        "The QROPS scheme manager details",
        "Tell us more about the QROPS scheme manager",
        routes.IndexController.onPageLoad(),
        TaskStatus.CannotStart
      ),
      TaskViewModel("submit", "Submit your form", "Check all your answers and submit the form", routes.IndexController.onPageLoad(), TaskStatus.CannotStart)
    )

    val taskListItems: Seq[TaskListItem] = tasks.map(TaskListViewModel.toTaskListItem)

    Ok(view(taskListItems))
  }
}

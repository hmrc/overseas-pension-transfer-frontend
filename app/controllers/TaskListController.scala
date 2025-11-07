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

import controllers.actions.{DataRetrievalAction, IdentifierAction, SchemeDataAction}
import controllers.helpers.ErrorHandling
import models.{SessionData, TransferId}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._
import repositories.SessionRepository
import services.{TaskService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.TaskListViewModel
import views.html.TaskListView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaskListController @Inject() (
    val controllerComponents: MessagesControllerComponents,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    schemeData: SchemeDataAction,
    sessionRepository: SessionRepository,
    view: TaskListView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with ErrorHandling {

  def onPageLoad(): Action[AnyContent] = (identify andThen schemeData andThen getData) { implicit request =>
    Ok(view(TaskListViewModel.rows(request.userAnswers), TaskListViewModel.submissionRow(request.userAnswers)))
  }

  def fromDashboard(transferId: TransferId): Action[AnyContent] = (identify andThen schemeData).async { implicit request =>
    val newSession = SessionData(
      request.authenticatedUser.internalId,
      transferId,
      request.schemeDetails,
      request.authenticatedUser,
      data = Json.obj()
    )

    sessionRepository.set(newSession) map {
      _ =>
        Redirect(controllers.routes.TaskListController.onPageLoad())
    }
  }
}

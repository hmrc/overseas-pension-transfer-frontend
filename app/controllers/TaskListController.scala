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

import controllers.actions.{DataRequiredAction, DataRetrievalAction, DisplayAction, IdentifierAction}
import models.NormalMode
import models.TaskCategory.QROPSDetails
import models.taskList.TaskStatus.Completed
import org.apache.pekko.Done
import pages.qropsDetails.QROPSDetailsSummaryPage
import play.api.i18n.I18nSupport
import play.api.mvc._
import queries.TaskStatusQuery
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
    requireData: DataRequiredAction,
    displayData: DisplayAction,
    sessionRepository: SessionRepository,
    taskService: TaskService,
    userAnswersService: UserAnswersService,
    view: TaskListView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData andThen displayData).async { implicit request =>
    for {
      ua            <- Future.fromTry(taskService.unblockSubmissionOnAllTasksCompletion(request.userAnswers))
      savedForLater <-
        if (ua == request.userAnswers) {
          Future.successful(Right(Done))
        } else {
          sessionRepository.set(ua).flatMap(_ => userAnswersService.setExternalUserAnswers(ua))
        }
    } yield {
      savedForLater match {
        case Right(Done) => Ok(view(TaskListViewModel.rows(request.userAnswers), TaskListViewModel.submissionRow(ua)))
        case _           => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }
  }
}

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

import connectors.TransferConnector
import controllers.actions.{DataRetrievalAction, IdentifierAction, SchemeDataAction}
import controllers.helpers.ErrorHandling
import models.{PstrNumber, QtStatus}
import models.dtos.UserAnswersDTO
import org.apache.pekko.Done
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.SessionRepository
import services.{TaskService, UserAnswersService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
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
    transferConnector: TransferConnector,
    view: TaskListView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with ErrorHandling {

  def onPageLoad(): Action[AnyContent] = (identify andThen schemeData andThen getData).async { implicit request =>
    for {
      sd1            <- Future.fromTry(TaskService.updateTaskStatusesOnMemberDetailsComplete(request.sessionData))
      sd2            <- Future.fromTry(TaskService.updateSubmissionTaskStatus(sd1))
      sessionUpdated <-
        if (sd2 == request.sessionData) {
          Future.successful(true)
        } else {
          sessionRepository.set(sd2)
        }
    } yield {
      if (sessionUpdated) {
        Ok(view(TaskListViewModel.rows(sd2), TaskListViewModel.submissionRow(sd2)))
      } else {
        onFailureRedirect("Session Repository unable to update Task List")
      }
    }
  }

  def continueJourney(referenceId: String, pstr: PstrNumber, qtStatus: QtStatus, versionNumber: Option[String]): Action[AnyContent] =
    (identify andThen schemeData).async { implicit request =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

      transferConnector
        .getSpecificTransfer(
          transferReference = Some(referenceId),
          pstrNumber        = pstr,
          qtStatus          = qtStatus
        )
        .flatMap {
          case Right(dto) =>
            val ua = UserAnswersDTO
              .toUserAnswers(dto)

            sessionRepository.set(ua).map {
              _ => Redirect(routes.TaskListController.onPageLoad())
            }

          case Left(_) =>
            Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        }
    }
}

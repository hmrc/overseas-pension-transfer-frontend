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

package controllers.actions

import models.{Mode, NormalMode, TaskCategory}
import models.requests.DataRequest
import models.taskList.TaskStatus.InProgress
import org.apache.pekko.Done
import play.api.mvc._
import play.api.mvc.Results.Redirect
import queries.TaskStatusQuery
import repositories.SessionRepository
import services.UserAnswersService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait MarkInProgressOnEntryAction {
  def forCategoryAndMode(category: TaskCategory, mode: Mode): ActionRefiner[DataRequest, DataRequest]
}

class MarkInProgressOnEntryActionImpl @Inject() (
    sessionRepository: SessionRepository,
    userAnswersService: UserAnswersService
  )(implicit ec: ExecutionContext
  ) extends MarkInProgressOnEntryAction {

  /** Sets a task as in progress for a category and mode. */
  override def forCategoryAndMode(category: TaskCategory, mode: Mode): ActionRefiner[DataRequest, DataRequest] =
    new ActionRefiner[DataRequest, DataRequest] {
      override protected def executionContext: ExecutionContext = ec

      override protected def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = {

        implicit val hc: HeaderCarrier =
          HeaderCarrierConverter.fromRequest(request)

        mode match {
          case NormalMode =>
            for {
              updated <- Future.fromTry(request.userAnswers.set(TaskStatusQuery(category), InProgress))
              _       <- sessionRepository.set(updated)
              saved   <- userAnswersService.setExternalUserAnswers(updated)
            } yield saved match {
              case Right(Done) => Right(request.copy(userAnswers = updated))
              case _           => Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
            }

          case _ => Future.successful(Right(request))
        }
      }
    }
}

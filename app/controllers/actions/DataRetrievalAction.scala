/*
 * Copyright 2024 HM Revenue & Customs
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

import controllers.routes
import models.requests.{DisplayRequest, IdentifierRequest}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import repositories.SessionRepository
import utils.AppUtils

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DataRetrievalActionImpl @Inject() (
    val sessionRepository: SessionRepository
  )(implicit val executionContext: ExecutionContext
  ) extends DataRetrievalAction with AppUtils {

  override protected def refine[A](request: IdentifierRequest[A]): Future[Either[Result, DisplayRequest[A]]] = {
    sessionRepository.get(request.authenticatedUser.internalId) map {
      case Some(answers) =>
        Right(DisplayRequest(
          request.request,
          request.authenticatedUser,
          answers,
          memberFullName(answers),
          qtNumber(answers),
          dateTransferSubmitted(answers)
        ))
      case None          => Left(Redirect(routes.JourneyRecoveryController.onPageLoad()))
    }
  }
}

trait DataRetrievalAction extends ActionRefiner[IdentifierRequest, DisplayRequest]

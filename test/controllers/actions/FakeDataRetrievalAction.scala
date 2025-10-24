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

import models.requests.{DisplayRequest, IdentifierRequest}
import models.{SessionData, UserAnswers}
import play.api.mvc.Result
import utils.AppUtils

import scala.concurrent.{ExecutionContext, Future}

class FakeDataRetrievalAction(answers: UserAnswers, sessionData: SessionData) extends DataRetrievalAction with AppUtils {

  override protected def refine[A](request: IdentifierRequest[A]): Future[Either[Result, DisplayRequest[A]]] = {

    Future(Right(DisplayRequest(
      request.request,
      request.authenticatedUser,
      answers,
      sessionData,
      memberFullName(sessionData),
      qtNumber(sessionData),
      dateTransferSubmitted(sessionData)
    )))
  }

  implicit override protected val executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
}

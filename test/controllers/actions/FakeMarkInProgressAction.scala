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

import models.{Mode, TaskCategory}
import models.requests.{DataRequest, DisplayRequest}
import play.api.mvc.{ActionRefiner, Result}

import scala.concurrent.{ExecutionContext, Future}

class FakeMarkInProgressAction extends MarkInProgressOnEntryAction {

  override def forCategoryAndMode(category: TaskCategory, mode: Mode): ActionRefiner[DisplayRequest, DisplayRequest] =
    new ActionRefiner[DisplayRequest, DisplayRequest] {

      override protected def refine[A](request: DisplayRequest[A]): Future[Either[Result, DisplayRequest[A]]] =
        Future.successful(Right(request))

      implicit override protected val executionContext: ExecutionContext =
        scala.concurrent.ExecutionContext.Implicits.global
    }
}

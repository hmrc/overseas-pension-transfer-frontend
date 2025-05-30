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

import models.requests.{DataRequest, DisplayRequest}
import play.api.mvc.ActionTransformer
import utils.AppUtils

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DisplayActionImpl @Inject() (implicit val executionContext: ExecutionContext) extends DisplayAction with AppUtils {

  override protected def transform[A](request: DataRequest[A]): Future[DisplayRequest[A]] = {
    Future.successful(
      DisplayRequest(request.request, request.userId, request.userAnswers, memberFullName(request.userAnswers), qtNumber(request.userAnswers))
    )
  }
}

trait DisplayAction extends ActionTransformer[DataRequest, DisplayRequest]

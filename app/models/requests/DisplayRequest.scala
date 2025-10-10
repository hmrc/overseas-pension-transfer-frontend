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

package models.requests

import models.{QtNumber, SessionData, UserAnswers}
import models.authentication.AuthenticatedUser
import play.api.mvc.{Request, WrappedRequest}

case class DisplayRequest[A](
    request: Request[A],
    authenticatedUser: AuthenticatedUser,
    userAnswers: UserAnswers,
    sessionData: SessionData,
    memberName: String,
    qtNumber: QtNumber,
    dateTransferSubmitted: String
  ) extends WrappedRequest[A](request)

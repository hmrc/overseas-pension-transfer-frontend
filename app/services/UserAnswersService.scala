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

package services

import com.google.inject.Inject
import connectors.UserAnswersConnector
import models.UserAnswers
import models.authentication.{AuthenticatedUser, PsaId}
import models.dtos.SubmissionDTO
import models.dtos.UserAnswersDTO.{fromUserAnswers, toUserAnswers}
import models.responses.{SubmissionErrorResponse, SubmissionResponse, UserAnswersError, UserAnswersNotFoundResponse}
import org.apache.pekko.Done
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class UserAnswersService @Inject() (
    connector: UserAnswersConnector
  )(implicit ec: ExecutionContext
  ) extends Logging {

  def getExternalUserAnswers(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Either[UserAnswersError, UserAnswers]] = {
    connector.getAnswers(userAnswers.id) map {
      case Right(userAnswersDTO)             => Right(toUserAnswers(userAnswersDTO))
      case Left(UserAnswersNotFoundResponse) => Right(UserAnswers(userAnswers.id, userAnswers.pstr))
      case Left(error)                       => Left(error)
    }
  }

  def setExternalUserAnswers(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Either[UserAnswersError, Done]] = {
    connector.putAnswers(fromUserAnswers(userAnswers))
  }

  def submitDeclaration(
      authenticatedUser: AuthenticatedUser,
      userAnswers: UserAnswers,
      maybePsaId: Option[PsaId] = None
    )(implicit hc: HeaderCarrier
    ): Future[Either[SubmissionErrorResponse, SubmissionResponse]] = {
    connector.postSubmission(SubmissionDTO.fromRequest(authenticatedUser, userAnswers, maybePsaId))
  }

  def clearUserAnswers(id: String)(implicit hc: HeaderCarrier): Future[Either[UserAnswersError, Done]] = {
    connector.deleteAnswers(id)
  }
}

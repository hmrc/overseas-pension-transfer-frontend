/*
 * Copyright 2023 HM Revenue & Customs
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

package connectors

import config.FrontendAppConfig
import connectors.parsers.UserAnswersParser.{
  DeleteUserAnswersType,
  GetSubmissionResponseHttpReads,
  GetUserAnswersHttpReads,
  GetUserAnswersType,
  SetUserAnswersHttpReads,
  SetUserAnswersType,
  SubmissionType
}
import models.dtos.{SubmissionDTO, UserAnswersDTO}
import models.responses.{SubmissionErrorResponse, UserAnswersErrorResponse}
import play.api.Logging
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import java.net.URL
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserAnswersConnector @Inject() (
    appConfig: FrontendAppConfig,
    http: HttpClientV2
  ) extends Logging {

  private def userAnswersUrl(id: String): URL =
    url"${appConfig.backendService}/save-for-later/$id"

  def getAnswers(transferId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[GetUserAnswersType] = {
    http.get(userAnswersUrl(transferId))
      .execute[GetUserAnswersType]
      .recover {
        case e: Exception =>
          logger.warn(s"Error retrieving user answers for ID '$transferId': ${e.getMessage}", e)
          Left(UserAnswersErrorResponse(e.toString, None))
      }
  }

  def putAnswers(
      userAnswersDTO: UserAnswersDTO
    )(implicit hc: HeaderCarrier,
      ec: ExecutionContext
    ): Future[SetUserAnswersType] = {
    http.post(userAnswersUrl(userAnswersDTO.referenceId))
      .withBody(Json.toJson(userAnswersDTO))
      .execute[SetUserAnswersType]
      .recover {
        case e: Exception =>
          logger.warn(s"Error updating user answers for ID '${userAnswersDTO.referenceId}': ${e.getMessage}", e)
          Left(UserAnswersErrorResponse(e.getMessage, None))
      }
  }

  private def submissionUrl(id: String): URL =
    url"${appConfig.backendService}/submit-declaration/$id"

  def postSubmission(submissionDTO: SubmissionDTO)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[SubmissionType] =
    http.post(submissionUrl(submissionDTO.referenceId))
      .withBody(Json.toJson(submissionDTO))
      .execute[SubmissionType]
      .recover {
        case e: Exception =>
          logger.warn(s"Error updating user answers for ID '${submissionDTO.referenceId}': ${e.getMessage}", e)
          Left(SubmissionErrorResponse(e.getMessage, None))
      }

  def deleteAnswers(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[DeleteUserAnswersType] = {
    def url: URL = url"${appConfig.backendService}/save-for-later/$id"

    http.delete(url)
      .execute[DeleteUserAnswersType]
      .recover {
        case e: Exception =>
          logger.warn(s"Error deleting user answers for ID '$id': ${e.getMessage}", e)
          Left(SubmissionErrorResponse(e.getMessage, None))
      }
  }
}

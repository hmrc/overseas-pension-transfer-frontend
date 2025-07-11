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
import connectors.parsers.GetUserAnswersParser.GetUserAnswersHttpReads
import connectors.parsers.SetUserAnswersParser.SetUserAnswersHttpReads
import connectors.parsers.UserAnswersParser.UserAnswersHttpReads
import models.dtos.UserAnswersDTO
import models.responses.{GetUserAnswersErrorResponse, GetUserAnswersResponse, SetUserAnswersErrorResponse, SetUserAnswersResponse, UserAnswersErrorResponse, UserAnswersResponse}
import uk.gov.hmrc.http.HttpResponse
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

  def getAnswers(transferId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[UserAnswersResponse] = {
    http.get(userAnswersUrl(transferId))
      .execute[UserAnswersResponse]
      .recover {
        case e: Exception =>
          logger.warn(s"Error retrieving user answers for ID '$transferId': ${e.getMessage}", e)
          UserAnswersErrorResponse(e.toString, None)
      }
  }

  def putAnswers(
      userAnswersDTO: UserAnswersDTO
    )(implicit hc: HeaderCarrier,
      ec: ExecutionContext
    ): Future[UserAnswersResponse] = {
    http.post(userAnswersUrl(userAnswersDTO.referenceId))
      .withBody(Json.toJson(userAnswersDTO))
      .execute[UserAnswersResponse]
      .recover {
        case e: Exception =>
          logger.warn(s"Error updating user answers for ID '${userAnswersDTO.referenceId}': ${e.getMessage}", e)
          UserAnswersErrorResponse(e.getMessage, None)
      }
  }

}

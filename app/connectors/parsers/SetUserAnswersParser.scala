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

package connectors.parsers

import models.responses.{SetUserAnswersErrorResponse, SetUserAnswersResponse, SetUserAnswersSuccessResponse}
import play.api.Logging
import play.api.http.Status.NO_CONTENT
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object SetUserAnswersParser {

  implicit object SetUserAnswersHttpReads extends HttpReads[SetUserAnswersResponse] with Logging {

    override def read(method: String, url: String, response: HttpResponse): SetUserAnswersResponse = {
      response.status match {
        case NO_CONTENT => SetUserAnswersSuccessResponse
        case statusCode =>
          logger.warn(s"[UserAnswersConnector][putUserAnswers] Error returned: downstreamStatus: $statusCode, responsePayload: ${response.json}")
          SetUserAnswersErrorResponse
      }
    }
  }
}

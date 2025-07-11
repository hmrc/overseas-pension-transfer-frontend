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

import models.dtos.UserAnswersDTO
import models.responses.{GetUserAnswersErrorResponse, GetUserAnswersNotFoundResponse, GetUserAnswersResponse, GetUserAnswersSuccessResponse}
import play.api.Logging
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json.{JsError, JsPath, JsSuccess, JsonValidationError}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object GetUserAnswersParser {

  implicit object GetUserAnswersHttpReads extends HttpReads[GetUserAnswersResponse] with Logging {

    override def read(method: String, url: String, response: HttpResponse): GetUserAnswersResponse = {
      response.status match {
        case OK         =>
          response.json.validate[UserAnswersDTO] match {
            case JsSuccess(value, _) => GetUserAnswersSuccessResponse(value)
            case JsError(errors)     =>
              logger.warn(s"[UserAnswersConnector][getUserAnswers] Unable to parse Json as UserAnswersDTO: ${formatJsonErrors(errors)}")
              GetUserAnswersErrorResponse(s"Unable to parse Json as UserAnswersDTO: ${formatJsonErrors(errors)}")
          }
        case NOT_FOUND  =>
          logger.warn("[UserAnswersConnector][getUserAnswers] No record was found in save for later}")
          GetUserAnswersNotFoundResponse
        case statusCode =>
          logger.warn(s"[UserAnswersConnector][getUserAnswers] Error returned: downstreamStatus: $statusCode, responsePayload: ${response.json}")
          GetUserAnswersErrorResponse("Error re")
      }
    }

    private val formatJsonErrors: scala.collection.Seq[(JsPath, scala.collection.Seq[JsonValidationError])] => String = {
      errors =>
        errors.map(_._1.toString()).mkString(" | ")

    }
  }
}

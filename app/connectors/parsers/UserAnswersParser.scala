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
import models.responses.{UserAnswersErrorResponse, UserAnswersNotFoundResponse, UserAnswersResponse, UserAnswersSaveSuccessfulResponse, UserAnswersSuccessResponse}
import play.api.Logging
import play.api.http.Status.{NOT_FOUND, NO_CONTENT, OK}
import play.api.libs.json.{JsError, JsPath, JsSuccess, JsonValidationError}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object UserAnswersParser {

  implicit object UserAnswersHttpReads extends HttpReads[UserAnswersResponse] with Logging {

    override def read(method: String, url: String, response: HttpResponse): UserAnswersResponse = {
      response.status match {
        case OK         =>
          response.json.validate[UserAnswersDTO] match {
            case JsSuccess(value, _) => UserAnswersSuccessResponse(value)
            case JsError(errors)     =>
              logger.warn(s"[UserAnswersConnector][getAnswers] Unable to parse Json as UserAnswersDTO: ${formatJsonErrors(errors)}")
              UserAnswersErrorResponse(s"Unable to parse Json as UserAnswersDTO", Some(formatJsonErrors(errors)))
          }
        case NO_CONTENT => UserAnswersSaveSuccessfulResponse
        case NOT_FOUND  =>
          logger.warn("[UserAnswersConnector][getAnswers] No record was found in save for later}")
          UserAnswersNotFoundResponse
        case statusCode =>
          response.json.validate[UserAnswersErrorResponse] match {
            case JsSuccess(value, _) =>
              logger.warn(s"[UserAnswersConnector][getAnswers] Error returned: downstreamStatus: $statusCode, error: ${value.error}")
              value
            case JsError(errors) =>
              logger.warn(s"[UserAnswersConnector][getAnswers] Unable to parse Json as UserAnswersDTO: ${formatJsonErrors(errors)}")
              UserAnswersErrorResponse(s"Unable to parse Json as UserAnswersErrorResponse", Some(formatJsonErrors(errors)))
          }
      }
    }

    private val formatJsonErrors: scala.collection.Seq[(JsPath, scala.collection.Seq[JsonValidationError])] => String = {
      errors =>
        errors.map(_._1.toString()).mkString(" | ")

    }
  }
}

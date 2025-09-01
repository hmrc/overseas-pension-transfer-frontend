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

import connectors.parsers.UserAnswersParser.GetUserAnswersHttpReads.logger
import models.dtos.{SubmissionDTO, UserAnswersDTO}
import models.responses.{SubmissionErrorResponse, SubmissionResponse, UserAnswersError, UserAnswersErrorResponse, UserAnswersNotFoundResponse}
import org.apache.pekko.Done
import play.api.Logging
import play.api.http.Status.{NOT_FOUND, NO_CONTENT, OK}
import play.api.libs.json.{JsError, JsPath, JsSuccess, JsonValidationError}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object UserAnswersParser {
  type GetUserAnswersType = Either[UserAnswersError, UserAnswersDTO]
  type SetUserAnswersType = Either[UserAnswersError, Done]
  type SubmissionType     = Either[SubmissionErrorResponse, SubmissionResponse]
  type DeleteUserAnswersType = Either[UserAnswersError, Done]

  implicit object GetUserAnswersHttpReads extends HttpReads[GetUserAnswersType] with Logging {

    override def read(method: String, url: String, response: HttpResponse): GetUserAnswersType =
      response.status match {
        case OK         =>
          response.json.validate[UserAnswersDTO] match {
            case JsSuccess(value, _) => Right(value)
            case JsError(errors)     =>
              logger.warn(s"[UserAnswersConnector][getAnswers] Unable to parse Json as UserAnswersDTO: ${formatJsonErrors(errors)}")
              Left(UserAnswersErrorResponse(s"Unable to parse Json as UserAnswersDTO", Some(formatJsonErrors(errors))))
          }
        case NOT_FOUND  =>
          logger.warn("[UserAnswersConnector][getAnswers] No record was found in save for later}")
          Left(UserAnswersNotFoundResponse)
        case statusCode =>
          response.json.validate[UserAnswersErrorResponse] match {
            case JsSuccess(value, _) =>
              logger.warn(s"[UserAnswersConnector][getAnswers] Error returned: downstreamStatus: $statusCode, error: ${value.error}")
              Left(value)
            case JsError(errors)     =>
              logger.warn(s"[UserAnswersConnector][getAnswers] Unable to parse Json as UserAnswersDTO: ${formatJsonErrors(errors)}")
              Left(UserAnswersErrorResponse("Unable to parse Json as UserAnswersErrorResponse", Some(formatJsonErrors(errors))))
          }
      }
  }

  implicit object SetUserAnswersHttpReads extends HttpReads[SetUserAnswersType] with Logging {

    override def read(method: String, url: String, response: HttpResponse): SetUserAnswersType =
      response.status match {
        case NO_CONTENT => Right(Done)
        case statusCode =>
          response.json.validate[UserAnswersErrorResponse] match {
            case JsSuccess(value, _) =>
              logger.warn(s"[UserAnswersConnector][putAnswers] Error returned: downstreamStatus: $statusCode, error: ${value.error}")
              Left(value)
            case JsError(errors)     =>
              logger.warn(s"[UserAnswersConnector][putAnswers] Unable to parse Json as UserAnswersDTO: ${formatJsonErrors(errors)}")
              Left(UserAnswersErrorResponse("Unable to parse Json as UserAnswersErrorResponse", Some(formatJsonErrors(errors))))
          }
      }
  }

  implicit object GetSubmissionResponseHttpReads extends HttpReads[SubmissionType] with Logging {

    override def read(method: String, url: String, response: HttpResponse): SubmissionType =
      response.status match {
        case OK         => response.json.validate[SubmissionResponse] match {
            case JsSuccess(value, _) => Right(value)
            case JsError(errors)     =>
              logger.warn(
                s"Response code: ${response.status} - [SubmissionConnector][postSubmission]" +
                  s" Unable to parse Json as SubmissionResponse: ${formatJsonErrors(errors)}"
              )
              Left(SubmissionErrorResponse(s"Unable to parse Json as SubmissionResponse", Some(formatJsonErrors(errors))))
          }
        case statusCode =>
          response.json.validate[SubmissionErrorResponse] match {
            case JsSuccess(value, _) =>
              logger.warn(
                s"Response code: ${response.status} - [SubmissionConnector][postSubmission]" +
                  s" Error returned: downstreamStatus: $statusCode, error: ${value.error}"
              )
              Left(value)
            case JsError(errors)     =>
              logger.warn(
                s"Response code: ${response.status} - [SubmissionConnector][postSubmission]" +
                  s" Unable to parse Json as SubmissionErrorResponse: ${formatJsonErrors(errors)}"
              )
              Left(SubmissionErrorResponse("Unable to parse Json as SubmissionErrorResponse", Some(formatJsonErrors(errors))))
          }
      }
  }

  private val formatJsonErrors: scala.collection.Seq[(JsPath, scala.collection.Seq[JsonValidationError])] => String = {
    errors =>
      errors.map(_._1.toString()).mkString(" | ")
  }
}

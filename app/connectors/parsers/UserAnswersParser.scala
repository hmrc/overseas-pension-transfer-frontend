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
import utils.DownstreamLogging

object UserAnswersParser {
  type GetUserAnswersType    = Either[UserAnswersError, UserAnswersDTO]
  type SetUserAnswersType    = Either[UserAnswersError, Done]
  type SubmissionType        = Either[SubmissionErrorResponse, SubmissionResponse]
  type DeleteUserAnswersType = Either[UserAnswersError, Done]

  implicit object GetUserAnswersHttpReads extends HttpReads[GetUserAnswersType] with Logging with DownstreamLogging {

    override def read(method: String, url: String, response: HttpResponse): GetUserAnswersType =
      response.status match {
        case OK         =>
          response.json.validate[UserAnswersDTO] match {
            case JsSuccess(value, _) => Right(value)
            case JsError(errors)     =>
              val formatted = formatJsonErrors(errors)
              logger.warn(s"[UserAnswersConnector][getAnswers] Unable to parse Json as UserAnswersDTO: $formatted")
              Left(UserAnswersErrorResponse("Unable to parse Json as UserAnswersDTO", Some(formatted)))
          }
        case NOT_FOUND  =>
          logger.warn("[UserAnswersConnector][getAnswers] No record was found in save for later}")
          Left(UserAnswersNotFoundResponse)
        case statusCode =>
          response.json.validate[UserAnswersErrorResponse] match {
            case JsSuccess(value, _) =>
              logger.warn(s"[UserAnswersConnector][getAnswers] Downstream error $statusCode: ${value.error}")
              Left(value)
            case JsError(errors)     =>
              val formatted = formatJsonErrors(errors)
              val err       = logBackendError("[UserAnswersConnector][getAnswers]", response)
              logger.warn(s"[UserAnswersConnector][getAnswers] Unable to parse Json as UserAnswersErrorResponse: $formatted")
              Left(UserAnswersErrorResponse(err.message, Some(err.body)))
          }
      }
  }

  implicit object SetUserAnswersHttpReads extends HttpReads[SetUserAnswersType] with Logging with DownstreamLogging {

    override def read(method: String, url: String, response: HttpResponse): SetUserAnswersType =
      response.status match {
        case NO_CONTENT => Right(Done)
        case statusCode =>
          response.json.validate[UserAnswersErrorResponse] match {
            case JsSuccess(value, _) =>
              logger.warn(s"[UserAnswersConnector][putAnswers] Downstream error $statusCode: ${value.error}")
              Left(value)
            case JsError(errors)     =>
              val formatted = formatJsonErrors(errors)
              val err       = logBackendError("[UserAnswersConnector][putAnswers]", response)
              logger.warn(s"[UserAnswersConnector][putAnswers] Unable to parse Json as UserAnswersErrorResponse: $formatted")
              Left(UserAnswersErrorResponse(err.message, Some(err.body)))
          }
      }
  }

  implicit object GetSubmissionResponseHttpReads extends HttpReads[SubmissionType] with Logging with DownstreamLogging {

    override def read(method: String, url: String, response: HttpResponse): SubmissionType =
      response.status match {
        case OK         => response.json.validate[SubmissionResponse] match {
            case JsSuccess(value, _) => Right(value)
            case JsError(errors)     =>
              val formatted = formatJsonErrors(errors)
              logger.warn(s"[SubmissionConnector][postSubmission] Unable to parse Json as SubmissionResponse: $formatted")
              Left(SubmissionErrorResponse("Unable to parse Json as SubmissionResponse", Some(formatted)))
          }
        case statusCode =>
          response.json.validate[SubmissionErrorResponse] match {
            case JsSuccess(value, _) =>
              logger.warn(s"[SubmissionConnector][postSubmission] Downstream error $statusCode: ${value.error}")
              Left(value)
            case JsError(errors)     =>
              val formatted = formatJsonErrors(errors)
              val err       = logBackendError("[SubmissionConnector][postSubmission]", response)
              logger.warn(s"[SubmissionConnector][postSubmission] Unable to parse Json as SubmissionErrorResponse: $formatted")
              Left(SubmissionErrorResponse(err.message, Some(err.body)))
          }
      }
  }

  implicit object DeleteUserAnswersHttpReads extends HttpReads[DeleteUserAnswersType] with Logging {

    override def read(method: String, url: String, response: HttpResponse): DeleteUserAnswersType =
      response.status match {
        case NO_CONTENT => Right(Done)
        case statusCode =>
          response.json.validate[UserAnswersErrorResponse] match {
            case JsSuccess(value, _) =>
              logger.warn(s"[UserAnswersConnector][deleteAnswers] Error returned: downstreamStatus: $statusCode, error: ${value.error}")
              Left(value)
            case JsError(errors)     =>
              logger.warn(s"[UserAnswersConnector][deleteAnswers] Unable to parse Json as UserAnswersDTO: ${formatJsonErrors(errors)}")
              Left(UserAnswersErrorResponse("Unable to parse Json as UserAnswersErrorResponse", Some(formatJsonErrors(errors))))
          }
      }
  }

  private val formatJsonErrors: scala.collection.Seq[(JsPath, scala.collection.Seq[JsonValidationError])] => String = {
    errors =>
      errors.map(_._1.toString()).mkString(" | ")
  }
}

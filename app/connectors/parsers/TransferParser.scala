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

import models.dtos.GetAllTransfersDTO
import models.responses._
import play.api.Logging
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.DownstreamLogging

object TransferParser {
  type GetAllTransfersType = Either[TransferError, GetAllTransfersDTO]

  implicit object GetAllTransfersHttpReads extends HttpReads[GetAllTransfersType] with Logging with DownstreamLogging {

    override def read(method: String, url: String, response: HttpResponse): GetAllTransfersType =
      response.status match {
        case OK                    =>
          response.json.validate[GetAllTransfersDTO] match {
            case JsSuccess(dto, _) =>
              val (valid, notValid) = dto.transfers.partition(_.isValid)
              if (notValid.nonEmpty) {
                logger.warn(s"[TransferConnector][getAllTransfers] Dropping ${notValid.size} invalid transfer items (must have exactly one of submissionDate or lastUpdated).")
              }
              Right(dto.copy(transfers = valid))
            case JsError(errors)     =>
              val formatted = formatJsonErrors(errors)
              logger.warn(s"[TransferConnector][getAllTransfers] Unable to parse Json as GetAllTransfersDTO: $formatted")
              Left(AllTransfersUnexpectedError("Unable to parse Json as GetAllTransfersDTO", Some(formatted)))
          }
        case NOT_FOUND             =>
          logger.warn("[TransferConnector][getAllTransfers] No record was found")
          Left(NoTransfersFound)
        case INTERNAL_SERVER_ERROR =>
          logger.warn("[TransferConnector][getAllTransfers] Something went wrong, see backend logs")
          Left(InternalServerError)
        case statusCode            =>
          logger.warn(s"[TransferConnector][getAllTransfers] Unexpected status code return: $statusCode")
          Left(AllTransfersUnexpectedError(s"Unexpected status code returned from backend: $statusCode", None))
      }
  }
}

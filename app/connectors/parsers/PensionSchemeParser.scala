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

import models.authentication.PsaId
import models.responses.{PensionSchemeError, PensionSchemeErrorResponse, PensionSchemeNotAssociated}
import models.{PensionSchemeDetails, PstrNumber, SrnNumber}
import play.api.Logging
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{__, JsError, JsSuccess, Reads}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.DownstreamLogging

object PensionSchemeParser {
  type PensionSchemeDetailsType = Either[PensionSchemeError, PensionSchemeDetails]
  type AuthorisingPsaIdType     = Either[PensionSchemeError, PsaId]

  implicit object GetPensionSchemeDetailsHttpReads extends HttpReads[PensionSchemeDetailsType] with Logging with DownstreamLogging {

    private val pensionSchemeDataFromApiReads: Reads[PensionSchemeDetails] = (
      (__ \ "srn").read[String].map(SrnNumber.apply) ~
        (__ \ "pstr").read[String].map(PstrNumber.apply) ~
        (__ \ "schemeName").read[String]
    )(PensionSchemeDetails.apply _)

    override def read(method: String, url: String, response: HttpResponse): PensionSchemeDetailsType =
      response.status match {
        case OK         =>
          response.json.validate[PensionSchemeDetails](pensionSchemeDataFromApiReads) match {
            case JsSuccess(value, _) => Right(value)
            case JsError(errors)     =>
              val formatted = formatJsonErrors(errors)
              logger.warn(s"[PensionSchemeConnector][getSchemeDetails] Unable to parse JSON as PensionSchemeData: $formatted")
              Left(PensionSchemeErrorResponse("Unable to parse JSON as PensionSchemeData", Some(formatted)))
          }
        case NOT_FOUND  => Left(new PensionSchemeNotAssociated)
        case statusCode => response.json.validate[PensionSchemeErrorResponse] match {
            case JsSuccess(value, _) =>
              logger.warn(s"[PensionSchemeConnector][getSchemeDetails] Downstream error $statusCode: ${value.error}")
              Left(value)
            case JsError(errors)     =>
              val formatted = formatJsonErrors(errors)
              val err       = logBackendError("[PensionSchemeConnector][getSchemeDetails]", response)
              logger.warn(s"[PensionSchemeConnector][getSchemeDetails] Unable to parse Json as PensionSchemeErrorResponse: $formatted")
              Left(PensionSchemeErrorResponse(err.message, Some(err.body)))
          }
      }
  }

  implicit object GetAuthorisingPsaIdHttpReads
      extends HttpReads[AuthorisingPsaIdType]
      with Logging
      with DownstreamLogging {

    private val authorisingPsaIdFromApiReads: Reads[PsaId] =
      (__ \ "pspDetails" \ "authorisingPSAID").read[String].map(PsaId.apply)

    override def read(method: String, url: String, response: HttpResponse): AuthorisingPsaIdType =
      response.status match {
        case OK         =>
          response.json.validate[PsaId](authorisingPsaIdFromApiReads) match {
            case JsSuccess(value, _) =>
              Right(value)
            case JsError(errors)     =>
              val formatted = formatJsonErrors(errors)
              logger.warn(s"[PensionSchemeConnector][getAuthorisingPsaId] Unable to parse JSON as AuthorisingPsaId: $formatted")
              Left(PensionSchemeErrorResponse("Unable to parse JSON as AuthorisingPsaId", Some(formatted)))
          }
        case NOT_FOUND  =>
          Left(new PensionSchemeNotAssociated)
        case statusCode =>
          response.json.validate[PensionSchemeErrorResponse] match {
            case JsSuccess(value, _) =>
              logger.warn(s"[PensionSchemeConnector][getAuthorisingPsaId] Downstream error $statusCode: ${value.error}")
              Left(value)
            case JsError(errors)     =>
              val formatted = formatJsonErrors(errors)
              val err       = logBackendError("[PensionSchemeConnector][getAuthorisingPsaId]", response)
              logger.warn(s"[PensionSchemeConnector][getAuthorisingPsaId] Unable to parse Json as PensionSchemeErrorResponse: $formatted")
              Left(PensionSchemeErrorResponse(err.message, Some(err.body)))
          }
      }
  }
}

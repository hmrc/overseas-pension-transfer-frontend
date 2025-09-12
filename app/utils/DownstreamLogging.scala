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

package utils

import play.api.Logging
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import models.BackendError
import play.api.libs.json.{JsPath, JsonValidationError}

trait DownstreamLogging extends Logging {

  val formatJsonErrors: scala.collection.Seq[(JsPath, scala.collection.Seq[JsonValidationError])] => String = {
    errors =>
      errors.map(_._1.toString()).mkString(" | ")
  }

  private def correlationFromResponse(response: HttpResponse): Option[String] =
    response.header("X-Request-ID")
      .orElse(response.header("X-Correlation-ID"))

  private def correlationFromHeaderCarrier(hc: HeaderCarrier): Option[String] =
    hc.requestId.map(_.value)

  /** Log a backend HTTP failure (non-2xx) and return BackendError for wrapping. */
  def logBackendError(origin: String, response: HttpResponse): BackendError = {
    val correlationId = correlationFromResponse(response).getOrElse("-")
    val reason        = response.header("Status").getOrElse("Unknown")
    val body          = response.body

    val err = BackendError(
      correlationId = correlationId,
      status        = response.status,
      reason        = reason,
      origin        = origin,
      body          = body
    )

    logger.error(s"Downstream failure: ${err.message} body=$body")
    err
  }

  /** Log an exception (network / mongo / timeouts etc) and produce a simple message */
  def logNonHttpError(origin: String, hc: HeaderCarrier, ex: Throwable): String = {
    val correlationId = correlationFromHeaderCarrier(hc).getOrElse("-")
    val message       = s"[$origin] non-http-exception (correlationId=$correlationId): ${ex.getMessage}"
    logger.error(message, ex)
    message
  }

}

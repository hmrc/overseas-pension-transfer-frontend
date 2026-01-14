/*
 * Copyright 2024 HM Revenue & Customs
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
import models.MinimalDetails
import models.authentication.{PsaId, PspId}
import play.api.Logging
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

sealed trait MinimalDetailsError

case object UpstreamError   extends MinimalDetailsError
case object DetailsNotFound extends MinimalDetailsError

class MinimalDetailsConnector @Inject() (appConfig: FrontendAppConfig, http: HttpClientV2) extends Logging {

  private val url = url"${appConfig.pensionAdministratorHost}/pension-administrator/get-minimal-details-self"

  def fetch(
      psaId: PsaId
    )(implicit hc: HeaderCarrier,
      ec: ExecutionContext
    ): Future[Either[MinimalDetailsError, MinimalDetails]] =
    fetch("psaId", psaId.value, loggedInAsPsa = true)

  def fetch(
      pspId: PspId
    )(implicit hc: HeaderCarrier,
      ec: ExecutionContext
    ): Future[Either[MinimalDetailsError, MinimalDetails]] =
    fetch("pspId", pspId.value, loggedInAsPsa = false)

  private def fetch(
      idType: String,
      idValue: String,
      loggedInAsPsa: Boolean
    )(implicit hc: HeaderCarrier,
      ec: ExecutionContext
    ): Future[Either[MinimalDetailsError, MinimalDetails]] =
    http
      .get(url)
      .setHeader(idType -> idValue, "loggedInAsPsa" -> loggedInAsPsa.toString)
      .execute[MinimalDetails]
      .map(Right(_))
      .recover {
        case e: NotFoundException if e.message.contains("no match found") =>
          Left(DetailsNotFound)
        case e                                                            =>
          logger.error(s"[MinimalDetailsConnector][fetch] Upstream error occurred ${e.getMessage}", e)
          Left(UpstreamError)
      }
}

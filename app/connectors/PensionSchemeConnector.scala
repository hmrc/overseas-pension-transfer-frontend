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

package connectors

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.parsers.PensionSchemeParser.{GetPensionSchemeDetailsHttpReads, PensionSchemeDetailsType}
import models.authentication.{AuthenticatedUser, PsaUser, PspUser}
import models.responses.PensionSchemeErrorResponse
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import utils.DownstreamLogging

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.{ExecutionContext, Future}

class PensionSchemeConnector @Inject() (
    appConfig: FrontendAppConfig,
    http: HttpClientV2
  )(implicit ec: ExecutionContext
  ) extends DownstreamLogging {

  def checkAssociation(srn: String, user: AuthenticatedUser)(implicit hc: HeaderCarrier): Future[Boolean] = {
    val url        = url"${appConfig.pensionSchemeService}/pensions-scheme/is-psa-associated"
    val userHeader = {
      user match {
        case PsaUser(psaId, _, _) => "psaId" -> psaId.value
        case PspUser(pspId, _, _) => "pspId" -> pspId.value
      }
    }

    http.get(url)
      .setHeader(
        "schemeReferenceNumber" -> srn,
        userHeader
      )
      .execute[Boolean]
  }

  def getSchemeDetails(srn: String, authenticatedUser: AuthenticatedUser)(implicit hc: HeaderCarrier): Future[PensionSchemeDetailsType] = {
    val (url, headers) = authenticatedUser match {
      case PsaUser(_, _, _) => (url"${appConfig.pensionSchemeService}/scheme/$srn", Seq("schemeIdType" -> "srn", "idNumber" -> srn))
      case PspUser(_, _, _) => (url"${appConfig.pensionSchemeService}/psp-scheme/$srn", Seq("srn" -> srn))
    }

    http.get(url)
      .setHeader(
        headers: _*
      )
      .execute[PensionSchemeDetailsType]
      .recover {
        case e: Exception =>
          val errMsg = logNonHttpError("[PensionSchemeConnector][getSchemeDetails]", hc, e)
          Left(PensionSchemeErrorResponse(errMsg, None))
      }
  }
}

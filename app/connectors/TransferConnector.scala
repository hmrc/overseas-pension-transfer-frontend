/*
 * Copyright 2023 HM Revenue & Customs
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

import models.responses.AllTransfersUnexpectedError
import utils.DownstreamLogging
import config.FrontendAppConfig
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.StringContextOps
import models.PstrNumber
import models.SrnNumber
import uk.gov.hmrc.http.client.HttpClientV2
import connectors.parsers.TransferParser.GetAllTransfersHttpReads
import connectors.parsers.TransferParser.GetAllTransfersType

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import javax.inject.Inject
import java.net.URL

class TransferConnector @Inject() (
  appConfig: FrontendAppConfig,
  http: HttpClientV2
)(implicit ec: ExecutionContext)
    extends Logging
    with DownstreamLogging {

  def getAllTransfers(srnNumber: SrnNumber, pstrNumber: PstrNumber)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[GetAllTransfersType] = {
    def allTransfersUrl: URL =
      url"${appConfig.backendService}/get-all-transfers/${pstrNumber.value}"

    http
      .get(allTransfersUrl)
      .setHeader("schemeReferenceNumber" -> srnNumber.value)
      .execute[GetAllTransfersType]
      .recover { case e: Exception =>
        val errMsg = logNonHttpError("[TransferConnector][getAllTransfers]", hc, e)
        Left(AllTransfersUnexpectedError(errMsg, None))
      }
  }
}

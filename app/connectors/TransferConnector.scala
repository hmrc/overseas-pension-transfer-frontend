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

import config.FrontendAppConfig
import connectors.parsers.TransferParser.{GetAllTransfersHttpReads, GetAllTransfersType}
import connectors.parsers.UserAnswersParser._
import models.PstrNumber
import models.responses.{AllTransfersUnexpectedError, UserAnswersErrorResponse}
import play.api.Logging
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import utils.DownstreamLogging

import java.net.URL
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TransferConnector @Inject() (
    appConfig: FrontendAppConfig,
    http: HttpClientV2
  )(implicit ec: ExecutionContext
  ) extends Logging with DownstreamLogging {

  def getAllTransfers(pstrNumber: PstrNumber)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[GetAllTransfersType] = {
    def allTransfersUrl: URL =
      url"${appConfig.backendService}/get-all-transfers/${pstrNumber.value}"

    http.get(allTransfersUrl)
      .execute[GetAllTransfersType]
      .recover {
        case e: Exception =>
          val errMsg = logNonHttpError("[TransferConnector][getAllTransfers]", hc, e)
          Left(AllTransfersUnexpectedError(errMsg, None))
      }
  }

  def getSpecificTransfer(
      transferReference: Option[String],
      qtNumber: Option[String]      = None,
      pstrNumber: String,
      qtStatus: String,
      versionNumber: Option[String] = None
    )(implicit hc: HeaderCarrier,
      ec: ExecutionContext
    ): Future[GetUserAnswersType] = {
    val referenceId = transferReference.orElse(qtNumber).getOrElse(throw new IllegalArgumentException(
      "getSpecificTransfer must have one of either transferReference or qtNumber"
    ))

    def allTransfersUrl: URL =
      url"${appConfig.backendService}/get-transfer/$referenceId"

    val queryStringParams =
      Seq("pstr" -> pstrNumber, "qtStatus" -> qtStatus) ++ versionNumber.toSeq.map("versionNumber" -> _)

    http.get(allTransfersUrl)
      .transform(_.addQueryStringParameters(queryStringParams: _*))
      .execute[GetUserAnswersType]
      .recover {
        case e: Exception =>
          val errMsg = logNonHttpError("[TransferConnector][getSpecificTransfer]", hc, e)
          Left(UserAnswersErrorResponse(errMsg, None))
      }
  }
}

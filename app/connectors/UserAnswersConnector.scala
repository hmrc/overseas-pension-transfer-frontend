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
import connectors.parsers.UserAnswersParser.{
  DeleteUserAnswersType,
  GetSubmissionResponseHttpReads,
  GetUserAnswersHttpReads,
  GetUserAnswersType,
  SetUserAnswersHttpReads,
  SetUserAnswersType,
  SubmissionType
}
import models.dtos.{SubmissionDTO, UserAnswersDTO}
import models.responses.{SubmissionErrorResponse, UserAnswersErrorResponse}
import models.{PstrNumber, QtNumber, QtStatus}
import play.api.Logging
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import utils.DownstreamLogging

import java.net.URL
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserAnswersConnector @Inject() (
    appConfig: FrontendAppConfig,
    http: HttpClientV2
  )(implicit ec: ExecutionContext
  ) extends Logging with DownstreamLogging {

  private def submissionUrl(id: String): URL =
    url"${appConfig.backendService}/submit-declaration/$id"

  // These two versions of getAnswers are purposely similar to one another as it is recommended to combine these two in a future refactor
  def getAnswers(transferId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[GetUserAnswersType] = {
    http.get(url"${appConfig.backendService}/save-for-later/$transferId")
      .execute[GetUserAnswersType]
      .recover {
        case e: Exception =>
          val errMsg = logNonHttpError("[UserAnswersConnector][getAnswers]", hc, e)
          Left(UserAnswersErrorResponse(errMsg, None))
      }
  }

  def getAnswers(
      transferReference: Option[String],
      qtNumber: Option[String]      = None,
      pstrNumber: PstrNumber,
      qtStatus: QtStatus,
      versionNumber: Option[String] = None
    )(implicit hc: HeaderCarrier,
      ec: ExecutionContext
    ): Future[GetUserAnswersType] = {
    val referenceId = transferReference.orElse(qtNumber).getOrElse(throw new IllegalArgumentException(
      "getSpecificTransfer must have one of either transferReference or qtNumber"
    ))

    def url: URL =
      url"${appConfig.backendService}/get-transfer/$referenceId"

    val queryStringParams = {
      Seq("pstr" -> pstrNumber.value, "qtStatus" -> qtStatus.toString) ++ versionNumber.toSeq.map("versionNumber" -> _)
    }

    http.get(url)
      .transform(_.addQueryStringParameters(queryStringParams: _*))
      .execute[GetUserAnswersType]
      .recover {
        case e: Exception =>
          val errMsg = logNonHttpError("[TransferConnector][getSpecificTransfer]", hc, e)
          Left(UserAnswersErrorResponse(errMsg, None))
      }
  }

  def putAnswers(
      userAnswersDTO: UserAnswersDTO
    )(implicit hc: HeaderCarrier,
      ec: ExecutionContext
    ): Future[SetUserAnswersType] = {
    http.post(url"${appConfig.backendService}/save-for-later")
      .withBody(Json.toJson(userAnswersDTO))
      .execute[SetUserAnswersType]
      .recover {
        case e: Exception =>
          val errMsg = logNonHttpError("[UserAnswersConnector][putAnswers]", hc, e)
          Left(UserAnswersErrorResponse(errMsg, None))
      }
  }

  def postSubmission(submissionDTO: SubmissionDTO)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[SubmissionType] =
    http.post(submissionUrl(submissionDTO.referenceId))
      .withBody(Json.toJson(submissionDTO))
      .execute[SubmissionType]
      .recover {
        case e: Exception =>
          val errMsg = logNonHttpError("[UserAnswersConnector][postSubmission]", hc, e)
          Left(SubmissionErrorResponse(errMsg, None))
      }

  def deleteAnswers(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[DeleteUserAnswersType] = {
    def url: URL = url"${appConfig.backendService}/save-for-later/$id"

    http.delete(url)
      .execute[DeleteUserAnswersType]
      .recover {
        case e: Exception =>
          logger.warn(s"Error deleting user answers for ID '$id': ${e.getMessage}", e)
          Left(SubmissionErrorResponse(e.getMessage, None))
      }
  }

  def resetDatabase(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val url = url"${appConfig.backendHost}/test-only/reset-test-data"
    http.delete(url)
      .execute[HttpResponse]
  }
}

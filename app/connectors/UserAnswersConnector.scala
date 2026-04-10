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
import connectors.parsers.UserAnswersParser.*
import models.dtos.{SubmissionDTO, UserAnswersDTO}
import models.{PstrNumber, QtStatus, SrnNumber, TransferId}
import play.api.Logging
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.*
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
  ) extends Logging
    with DownstreamLogging {

  private def submissionUrl(id: String): URL =
    url"${appConfig.backendService}/submit-declaration/$id"

  // These two versions of getAnswers are purposely similar to one another as it is recommended to combine these two in a future refactor
  def getAnswers(
      transferId: String,
      srnNumber: SrnNumber
    )(implicit hc: HeaderCarrier,
      ec: ExecutionContext
    ): Future[GetUserAnswersType] =
    http
      .get(url"${appConfig.backendService}/save-for-later/$transferId")
      .setHeader("schemeReferenceNumber" -> srnNumber.value)
      .execute[GetUserAnswersType]

  def getAnswers(
      transferId: TransferId,
      pstrNumber: PstrNumber,
      qtStatus: QtStatus,
      versionNumber: Option[String] = None,
      srnNumber: SrnNumber
    )(implicit hc: HeaderCarrier,
      ec: ExecutionContext
    ): Future[GetUserAnswersType] = {

    def url: URL =
      url"${appConfig.backendService}/get-transfer/${transferId.value}"

    val queryStringParams =
      Seq("pstr" -> pstrNumber.value, "qtStatus" -> qtStatus.toString) ++ versionNumber.toSeq.map("versionNumber" -> _)

    http
      .get(url)
      .transform(_.addQueryStringParameters(queryStringParams: _*))
      .setHeader("schemeReferenceNumber" -> srnNumber.value)
      .execute[GetUserAnswersType]
  }

  def putAnswers(
      userAnswersDTO: UserAnswersDTO,
      srnNumber: SrnNumber
    )(implicit hc: HeaderCarrier,
      ec: ExecutionContext
    ): Future[SetUserAnswersType] =
    http
      .post(url"${appConfig.backendService}/save-for-later")
      .setHeader("schemeReferenceNumber" -> srnNumber.value)
      .withBody(Json.toJson(userAnswersDTO))
      .execute[SetUserAnswersType](SetUserAnswersHttpReads, ec)

  def postSubmission(
      submissionDTO: SubmissionDTO,
      srnNumber: SrnNumber
    )(implicit hc: HeaderCarrier,
      ec: ExecutionContext
    ): Future[SubmissionType] =
    http
      .post(submissionUrl(submissionDTO.referenceId.value))
      .setHeader("schemeReferenceNumber" -> srnNumber.value)
      .withBody(Json.toJson(submissionDTO))
      .execute[SubmissionType]

  def deleteAnswers(
      id: String,
      srnNumber: SrnNumber
    )(implicit hc: HeaderCarrier,
      ec: ExecutionContext
    ): Future[DeleteUserAnswersType] = {
    def url: URL = url"${appConfig.backendService}/save-for-later/$id"

    http
      .delete(url)
      .setHeader("schemeReferenceNumber" -> srnNumber.value)
      .execute[DeleteUserAnswersType](DeleteUserAnswersHttpReads, ec)
  }

  def resetDatabase(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    def url: URL = url"${appConfig.backendHost}/test-only/reset-test-data"

    http
      .delete(url)
      .execute[HttpResponse]
  }
}

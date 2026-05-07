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

import cats.data.EitherT
import config.FrontendAppConfig
import models.email.EmailToSendRequest
import play.api.Logging
import play.api.libs.json.Json
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.http.*
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailConnector @Inject() (
    appConfig: FrontendAppConfig,
    httpClientV2: HttpClientV2,
    httpClientResponse: HttpClientResponse
  ) extends Logging {

  def send(
      email: EmailToSendRequest
    )(implicit ec: ExecutionContext,
      hc: HeaderCarrier
    ): EitherT[Future, UpstreamErrorResponse, HttpResponse] = {
    httpClientResponse.read(
      httpClientV2
        .post(url"${appConfig.emailService}")
        .withBody(Json.toJson(email))
        .execute[Either[UpstreamErrorResponse, HttpResponse]]
    )
  }
}

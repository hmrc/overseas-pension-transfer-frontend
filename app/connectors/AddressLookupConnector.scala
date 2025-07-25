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
import models.address._
import models.responses.{AddressLookupErrorResponse, AddressLookupResponse, AddressLookupSuccessResponse}
import play.api.Logging
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpReads.Implicits.readJsValue
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import java.net.URL
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddressLookupConnector @Inject() (appConfig: FrontendAppConfig, val http: HttpClientV2) extends Logging {

  private val lookupUrl: URL = url"${appConfig.addressLookupService}/lookup"

  def lookup(postcode: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AddressLookupResponse] = {
    http.post(lookupUrl)
      .withBody(Json.obj("postcode" -> postcode))
      .setHeader("X-Hmrc-Origin" -> "PODS")
      .execute[JsValue] map {
      addressListJson =>
        AddressLookupSuccessResponse(postcode, addressListJson.as[Seq[AddressRecord]])
    } recover {
      case e: Exception =>
        logger.warn(s"Error received from address lookup service: $e")
        AddressLookupErrorResponse(e)
    }
  }
}

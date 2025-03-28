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

import com.google.inject.Inject
import config.FrontendAppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import scala.concurrent.{ExecutionContext, Future}

//TODO: Look into using HttpClientV2
class HelloWorldConnector @Inject() (http: HttpClient, appConfig: FrontendAppConfig) {

  def getHelloWorld()(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[String] = {
    val url = s"${appConfig.backendService}/hello-world"
    http.GET[String](url)
  }
}

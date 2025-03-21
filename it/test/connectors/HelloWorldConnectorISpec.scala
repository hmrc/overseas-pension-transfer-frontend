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

import base.BaseISpec
import org.apache.pekko.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import play.api.Logging
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.test.Injecting
import stubs.HelloWorldStub
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

class HelloWorldConnectorISpec extends BaseISpec with Injecting {

  val connector: HelloWorldConnector = inject[HelloWorldConnector]

  "HelloWorldConnector" should {

    "return 'Hello world!' when the backend responds with 200 OK" in {
      val expectedResponse = "Hello world!"

      HelloWorldStub.stubResponse(HelloWorldStub.helloWorldUri)(OK, Json.toJson(expectedResponse))

      val result = await(connector.getHelloWorld)

      result shouldBe expectedResponse
    }
  }
}

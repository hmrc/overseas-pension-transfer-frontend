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

package controllers.actions

import connectors.HelloWorldConnector
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

// THIS IS NOT GOOD PRACTICE AND IS ONLY HERE BECAUSE A FULL IMPLEMENTATION IS BEYOND THE SCOPE OF THIS TICKET
class FakeHelloWorldConnector extends HelloWorldConnector(null, null) {

  override def getHelloWorld()(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[String] = {
    Future.successful("Hello world!")
  }
}

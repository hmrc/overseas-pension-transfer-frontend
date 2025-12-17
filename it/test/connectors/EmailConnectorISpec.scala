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

import base.BaseISpec
import models.email.{EMAIL_ACCEPTED, EMAIL_NOT_SENT, EmailToSendRequest, SubmissionConfirmation}
import play.api.libs.json.Json
import play.api.test.Injecting
import play.api.http.Status.{ACCEPTED, BAD_GATEWAY, GATEWAY_TIMEOUT}
import stubs.EmailStub

import scala.concurrent.ExecutionContext.Implicits.global

class EmailConnectorISpec extends BaseISpec with Injecting {

  val connector: EmailConnector = inject[EmailConnector]

  private val emailRequest: EmailToSendRequest =
    EmailToSendRequest(
      to         = List("test.user@example.com"),
      templateId = "submitted-confirmation-template",
      parameters = SubmissionConfirmation(
        recipientName = "Test User",
        amendmentDate = "17 December 2025"
      )
    )

  "EmailConnector.send" when {
    "sending an email" must {
      "return EMAIL_ACCEPTED when downstream responds 202" in {
        EmailStub.sendEmailResponse(
          status                 = ACCEPTED,
          expectedRequestBodyJson = Json.stringify(Json.toJson(emailRequest))
        )

        await(connector.send(emailRequest)) shouldBe EMAIL_ACCEPTED
      }

      "return EMAIL_NOT_SENT when downstream responds 502 Bad Gateway" in {
        EmailStub.sendEmailResponse(
          status                 = BAD_GATEWAY,
          expectedRequestBodyJson = Json.stringify(Json.toJson(emailRequest))
        )

        await(connector.send(emailRequest)) shouldBe EMAIL_NOT_SENT
      }

      "return EMAIL_NOT_SENT when downstream responds 504 Gateway Timeout" in {
        EmailStub.sendEmailResponse(
          status                 = GATEWAY_TIMEOUT,
          expectedRequestBodyJson = Json.stringify(Json.toJson(emailRequest))
        )

        await(connector.send(emailRequest)) shouldBe EMAIL_NOT_SENT
      }
    }
  }
}

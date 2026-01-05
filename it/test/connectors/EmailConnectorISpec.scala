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
import models.email.{EmailAccepted, EmailNotSent, EmailToSendRequest, SubmissionConfirmation}
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
        qtReference       = "QT123456",
        memberName        = "Foo Bar",
        submissionDate    = "3 October 2025 at 3:00pm",
        pensionSchemeName = "Smith Harper Pension Scheme"
      )
    )

  "EmailConnector.send" when {
    "sending an email" must {
      "return EMAIL_ACCEPTED when downstream responds 202" in {
        EmailStub.sendEmailResponse(
          status                 = ACCEPTED,
          expectedRequestBodyJson = Json.stringify(Json.toJson(emailRequest))
        )

        await(connector.send(emailRequest)) shouldBe EmailAccepted
      }

      "return EMAIL_NOT_SENT when downstream responds 502 Bad Gateway" in {
        EmailStub.sendEmailResponse(
          status                 = BAD_GATEWAY,
          expectedRequestBodyJson = Json.stringify(Json.toJson(emailRequest))
        )

        await(connector.send(emailRequest)) shouldBe EmailNotSent
      }

      "return EMAIL_NOT_SENT when downstream responds 504 Gateway Timeout" in {
        EmailStub.sendEmailResponse(
          status                 = GATEWAY_TIMEOUT,
          expectedRequestBodyJson = Json.stringify(Json.toJson(emailRequest))
        )

        await(connector.send(emailRequest)) shouldBe EmailNotSent
      }
    }
  }
}

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

package viewmodels

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.Helpers.stubMessagesApi

class FooterLinkSpec extends AnyWordSpec with Matchers {

  implicit val messages: Messages = stubMessagesApi().preferred(Seq.empty)

  "FooterLink Build" should {

    "return only returnDashboardLink when showStartFooter is true" in {
      val result = FooterLink.build(showStartFooter = true)

      result           should have length 1
      result.head.id shouldBe "returnDashboardLink"
    }

    "return only returnDashboardLink when showCYAFooter is true" in {
      val result = FooterLink.build(showCYAFooter = true)

      result           should have length 1
      result.head.id shouldBe "returnDashboardLink"
    }

    "return discardReportLink and returnDashboardLink when showTaskListFooter is true" in {
      val result = FooterLink.build(showTaskListFooter = true)

      result           should have length 2
      result.map(_.id) should contain allOf ("discardReportLink", "returnDashboardLink")
    }

    "return returnTaskListLink when only showPageFooter is true" in {
      val result = FooterLink.build(showPageFooter = true)

      result           should have length 1
      result.head.id shouldBe "returnTaskListLink"
    }

    "return nothing if all flags are false" in {
      val result = FooterLink.build(showPageFooter = false)

      result shouldBe empty
    }
  }
}

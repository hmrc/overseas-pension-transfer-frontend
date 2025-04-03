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

package viewmodels.checkAnswers.memberDetails

import base.SpecBase
import models.address._
import pages.MembersCurrentAddressPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class MembersCurrentAddressSummarySpec extends SpecBase {

  "MembersCurrentAddress Summary" - {
    implicit val messages: Messages = stubMessages()

    "must return a row with all fields present" in {
      val address = MembersCurrentAddress("Line1", "Line2", Some("Line3"), Some("Town"), Some("Country"), Some("Postcode"))

      val answers = emptyUserAnswers.set(MembersCurrentAddressPage, address).success.value

      val row = MembersCurrentAddressSummary.row(answers)

      row mustBe defined
      row.get.key.content.asHtml.body must include("membersCurrentAddress.checkYourAnswersLabel")
      row.get.value.content.asHtml.body must include("Line1<br>Line2<br>Line3<br>Town<br>Country<br>Postcode")
    }

    "must return a row with only required fields present" in {
      val address = MembersCurrentAddress(
        addressLine1 = "Line 1",
        addressLine2 = "Line 2",
        addressLine3 = None,
        townOrCity   = None,
        country      = None,
        postcode     = None
      )

      val answers = emptyUserAnswers.set(MembersCurrentAddressPage, address).success.value

      val row = MembersCurrentAddressSummary.row(answers)

      row mustBe defined
      row.get.value.content.asHtml.body must include("Line 1<br>Line 2")
      row.get.value.content.asHtml.body must not include "null"
    }

    "must not include blank or whitespace-only fields" in {
      val address = MembersCurrentAddress(
        addressLine1 = "Line 1",
        addressLine2 = "  ",
        addressLine3 = Some(""),
        townOrCity   = Some("City"),
        country      = None,
        postcode     = Some(" ")
      )

      val answers = emptyUserAnswers.set(MembersCurrentAddressPage, address).success.value

      val row = MembersCurrentAddressSummary.row(answers)

      row mustBe defined
      row.get.value.content.asHtml.body must include("Line 1<br>City")
      row.get.value.content.asHtml.body must not include "<br><br>"
    }

    "return None when address is not present" in {
      val row = MembersCurrentAddressSummary.row(emptyUserAnswers)
      row mustBe None
    }
  }
}

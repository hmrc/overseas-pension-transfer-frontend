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
import models.address.MembersLastUKAddress
import org.scalatest.freespec.AnyFreeSpec
import pages.memberDetails.MembersLastUKAddressPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class MembersLastUKAddressSummarySpec extends AnyFreeSpec with SpecBase {

  "MembersLastUkAddress Summary" - {
    implicit val messages: Messages = stubMessages()

    "must return a row with all fields present" in {
      val address = MembersLastUKAddress("Line1", "Line2", Some("Line3"), Some("Line4"), "Postcode")

      val answers = emptyUserAnswers.set(MembersLastUKAddressPage, address).success.value

      val row = MembersLastUKAddressSummary.row(answers)

      row mustBe defined
      row.get.key.content.asHtml.body must include("membersLastUKAddress.checkYourAnswersLabel")
      row.get.value.content.asHtml.body must include("Line1<br>Line2<br>Line3<br>Line4<br>Postcode")
    }

    "must return a row with only required fields present" in {
      val address = MembersLastUKAddress(
        addressLine1 = "Line1",
        addressLine2 = "Line2",
        addressLine3 = None,
        addressLine4 = None,
        rawPostcode  = "Postcode"
      )

      val answers = emptyUserAnswers.set(MembersLastUKAddressPage, address).success.value

      val row = MembersLastUKAddressSummary.row(answers)

      row mustBe defined
      row.get.value.content.asHtml.body must include("Line1<br>Line2<br>Postcode")
      row.get.value.content.asHtml.body must not include "null"
    }
// TODO: This test should be fixed once it is decided how to handle whitespace in optional fields

//    "must not include blank or whitespace-only fields" in {
//      val address = MembersLastUKAddress(
//        addressLine1 = "Line1",
//        addressLine2 = "Line2",
//        addressLine3 = Some("    "),
//        addressLine4 = Some(""),
//        rawPostcode  = "Postcode"
//      )
//
//      val answers = emptyUserAnswers.set(MembersLastUKAddressPage, address).success.value
//
//      val row = MembersLastUKAddressSummary.row(answers)
//
//      row mustBe defined
//      row.get.value.content.asHtml.body must include("Line1<br>Line2<br>Postcode")
//      row.get.value.content.asHtml.body must not include "<br><br>"
//    }

    "return None when address is not present" in {
      val row = MembersLastUKAddressSummary.row(emptyUserAnswers)
      row mustBe None
    }
  }
}

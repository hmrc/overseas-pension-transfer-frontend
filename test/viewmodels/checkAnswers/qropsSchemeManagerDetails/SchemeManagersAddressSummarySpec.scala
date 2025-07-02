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

package viewmodels.checkAnswers.qropsSchemeManagerDetails

import base.SpecBase
import models.address.{BaseAddress, Country, SchemeManagersAddress}
import org.scalatest.freespec.AnyFreeSpec
import pages.qropsSchemeManagerDetails.SchemeManagersAddressPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class SchemeManagersAddressSummarySpec extends AnyFreeSpec with SpecBase {

  "Scheme manager's address Summary" - {
    implicit val messages: Messages = stubMessages()

    "must return a row with all fields present" in {
      val address = SchemeManagersAddress(
        BaseAddress(
          line1   = "Line1",
          line2   = "Line2",
          line3   = Some("Line3"),
          line4   = Some("Line4"),
          line5   = Some("Line5"),
          country = Country("FI", "Finland")
        )
      )

      val answers = emptyUserAnswers.set(SchemeManagersAddressPage, address).success.value
      val row     = SchemeManagersAddressSummary.row(answers)

      row mustBe defined
      row.get.key.content.asHtml.body must include("schemeManagersAddress.checkYourAnswersLabel")
      row.get.value.content.asHtml.body must include("Line1<br>Line2<br>Line3<br>Line4<br>Line5<br>Finland")
    }

    "must return a row with only required fields present" in {
      val address = SchemeManagersAddress(
        BaseAddress(
          line1   = "Line1",
          line2   = "Line2",
          country = Country("FI", "Finland")
        )
      )

      val answers = emptyUserAnswers.set(SchemeManagersAddressPage, address).success.value
      val row     = SchemeManagersAddressSummary.row(answers)

      row mustBe defined
      row.get.value.content.asHtml.body must include("Line1<br>Line2<br>Finland")
      row.get.value.content.asHtml.body must not include "null"
    }

    "must not include blank or whitespace-only fields" in {
      val address = SchemeManagersAddress(
        BaseAddress(
          line1   = "Line1",
          line2   = "Line2",
          line3   = Some("    "),
          line4   = Some(""),
          line5   = Some("  "),
          country = Country("FI", "Finland")
        )
      )

      val answers = emptyUserAnswers.set(SchemeManagersAddressPage, address).success.value
      val row     = SchemeManagersAddressSummary.row(answers)

      row mustBe defined
      row.get.value.content.asHtml.body must include("Line1<br>Line2<br>Finland")
      row.get.value.content.asHtml.body must not include "<br><br>"
    }

    "return None when address is not present" in {
      val row = SchemeManagersAddressSummary.row(emptyUserAnswers)
      row mustBe None
    }
  }
}

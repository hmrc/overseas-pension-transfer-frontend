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

package viewmodels.checkAnswers.qropsDetails

import base.SpecBase
import models.CheckMode
import models.address.{Country, QROPSAddress}
import org.scalatest.freespec.AnyFreeSpec
import pages.qropsDetails.QROPSAddressPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}

class QROPSAddressSummarySpec extends AnyFreeSpec with SpecBase {

  "QROPSAddressSummary" - {
    implicit val messages: Messages = stubMessages()

    "must return a SummaryListRow when QROPSAddressPage has a value" in {
      val answers = emptyUserAnswers.set(QROPSAddressPage, QROPSAddress("Line1", "Line2", None, None, None, Country("GB", "United Kingdom"))).success.value
      val result  = QROPSAddressSummary.row(CheckMode, answers)

      result mustBe defined
      result.get.key.content mustBe Text(messages("qropsAddress.checkYourAnswersLabel"))
      result.get.value.content mustBe HtmlContent("Line1<br>Line2<br>United Kingdom")
      result.get.value.content.asHtml.body must not include "<br><br>"
      result.get.actions.get.items.head.href mustBe
        controllers.qropsDetails.routes.QROPSAddressController.onPageLoad(CheckMode).url
    }
  }
}

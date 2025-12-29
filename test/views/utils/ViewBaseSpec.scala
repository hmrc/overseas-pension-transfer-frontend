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

package views.utils

import base.SpecBase
import org.apache.pekko.stream.Materializer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.Html
import uk.gov.hmrc.http.HeaderCarrier

trait ViewBaseSpec extends AnyFreeSpec with SpecBase {

  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  implicit lazy val messagesApi: MessagesApi = applicationBuilder().injector.instanceOf(classOf[MessagesApi])
  implicit lazy val messages: Messages       = messagesApi.preferred(FakeRequest())

  implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  implicit val lang: Lang        = mock[Lang]
  implicit val mat: Materializer = mock[Materializer]

  val mockMcc = mock[MessagesControllerComponents]

  def doc(body: String): Document = Jsoup.parse(body)

  def pageWithBackLink(html: Html): Unit =
    "have a back link" in {
      assert(
        doc(html.body).getElementsByClass("govuk-back-link") != null,
        "\n\nElement " + "govuk-back-link" + " was not rendered on the page.\n"
      )
    }

  def pageWithErrors(view: Html, idSelectorPrefix: String, errorMessage: String): Unit =
    s"show errors correctly for $idSelectorPrefix field" in {
      doc(view.body).getElementsByClass("govuk-error-summary__list").first().text() must include(messages(errorMessage))

      doc(view.body).getElementById(s"$idSelectorPrefix-error").text() must include(messages(errorMessage))

    }

  def pageWithTitle(view: Html, message: String): Unit =
    "show correct page title" in {
      doc(view.body).getElementsByTag("title").first().text mustBe s"${messages(message)} - ${messages("service.name")} - GOV.UK"
    }

  def pageWithH1(view: Html, message: String): Unit =
    "show correct h1" in {
      doc(view.body).getElementsByTag("h1").text() mustBe messages(message)
    }

  def pageWithHeadings(view: Html, headerLevel: String, message: String*): Unit =
    "show correct headings" in {
      doc(view.body).getElementById("main-content").getElementsByTag(headerLevel).eachText().toArray mustBe message.map(messageKey =>
        messages(messageKey)
      ).toArray
    }

  def pageWithLinks(view: Html, expectedLink: (String, String)*): Unit =
    s"show correct links" in {
      val links = doc(view.body).getElementById("main-content").getElementsByTag("a")

      expectedLink.zipWithIndex.map {
        case ((message, href), index) =>
          links.get(index).text() mustBe messages(message)
          links.get(index).attribute("href").getValue mustBe href
      }
    }

  def pageWithText(view: Html, expectedText: String*): Unit =
    s"show correct text: $expectedText" in {
      doc(view.body).getElementById("main-content").getElementsByTag("p").eachText().toArray mustBe
        expectedText.map(messageKey =>
          messages(messageKey)
        ).toArray
    }

  def pageWithRadioButtons(view: Html, expectedText: String*): Unit =
    "show correct radio buttons" in {
      doc(view.body).getElementsByClass("govuk-radios__item").eachText().toArray() mustBe
        expectedText.map(messageKey =>
          messages(messageKey)
        ).toArray
    }

}

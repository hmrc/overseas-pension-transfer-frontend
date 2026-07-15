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
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessagesApi

class DisplayItemsSpec extends AnyWordSpec with Matchers {

  implicit val messages: Messages =
    stubMessagesApi(
      Map(
        "en" -> Map(
          "base.text.title.noItems"       -> "No items",
          "base.text.title.oneItem"       -> "One item",
          "base.text.title.multipleItems" -> "Multiple items"
        )
      )
    ).preferred(Seq.empty)

  "describeItems" should {
    "return correct message when zero items" in {
      val result = DisplayItems.describeItems(Nil, "base")
      result shouldBe "No items"
    }
    "return correct message when one item" in {
      val result = DisplayItems.describeItems(Seq(""), "base")
      result shouldBe "One item"
    }
    "return correct message when >1 items" in {
      val result = DisplayItems.describeItems(Seq("", ""), "base")
      result shouldBe "Multiple items"
    }
  }
}

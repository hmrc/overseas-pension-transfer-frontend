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

package models

import base.SpecBase
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.SubmitToHMRCPage
import play.api.libs.json._

import java.time.Instant

class UserAnswersSpec extends AnyFreeSpec with Matchers with SpecBase {

  private val instant = Instant.now()

  "get" - {
    "should get Some value from data Json when value is present" in {
      emptyUserAnswers.copy(data = Json.obj("submitToHMRC" -> true, "key" -> "value")).get(SubmitToHMRCPage) mustBe
        Some(true)
    }

    "should get None when no value present in data Json" in {
      emptyUserAnswers.get(SubmitToHMRCPage) mustBe None
    }
  }

  "set" - {
    "should update Json in data field" in {
      emptyUserAnswers.set(SubmitToHMRCPage, false).success.value.data mustBe
        Json.obj("submitToHMRC" -> false)
    }
  }

  "remove" - {
    "should remove existing Json from data field" in {
      emptyUserAnswers.copy(data = Json.obj("submitToHMRC" -> true, "key" -> "value"))
        .remove(SubmitToHMRCPage).success.value.data mustBe
        Json.obj("key" -> "value")
    }
  }
}

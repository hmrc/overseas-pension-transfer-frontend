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

package forms.transferDetails

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class AdditionalUnquotedShareFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "additionalUnquotedShare.error.required"

  val form = new AdditionalUnquotedShareFormProvider()()

  ".value" - {

    val fieldName = "add-another"

    "bind true when the value is 'Yes'" in {
      val result = form.bind(Map(fieldName -> "Yes"))
      result.get mustBe true
    }

    "bind false when the value is 'No'" in {
      val result = form.bind(Map(fieldName -> "No"))
      result.get mustBe false
    }

    "error when value is missing" in {
      val result = form.bind(Map.empty[String, String])
      result.errors must contain(FormError(fieldName, requiredKey))
    }
  }
}

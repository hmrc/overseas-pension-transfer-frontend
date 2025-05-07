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

package forms.memberDetails

import forms.FormSpec
import play.api.data.Form
import play.api.data.FormError

class MemberConfirmLastUkAddressFormProviderSpec extends FormSpec {

  private val form: Form[Boolean] = new MemberConfirmLastUkAddressFormProvider()()

  ".value" - {

    "must bind true when the field is omitted (default=true)" in {
      val result = form.bind(Map.empty[String, String])
      result.errors mustBe empty
      result.value mustBe Some(true)
    }

    "must bind true if provided as 'true'" in {
      val result = form.bind(Map("value" -> "true"))
      result.errors mustBe empty
      result.value mustBe Some(true)
    }

    "must fail if provided a non-boolean" in {
      val result = form.bind(Map("value" -> "notABoolean"))
      result.errors mustBe Seq(FormError("value", "error.boolean"))
    }
  }
}

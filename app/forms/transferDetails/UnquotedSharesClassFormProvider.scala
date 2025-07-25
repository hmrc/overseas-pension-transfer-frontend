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

import forms.mappings.{Mappings, Regex}
import play.api.data.Form

import javax.inject.Inject

class UnquotedSharesClassFormProvider @Inject() extends Mappings with Regex {

  def apply(): Form[String] =
    Form(
      "value" -> text("unquotedSharesClass.error.required")
        .verifying(maxLength(160, "unquotedSharesClass.error.length"))
        .verifying(regexp(classRegex, "unquotedSharesClass.error.pattern"))
    )
}

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

package forms.qropsSchemeManagerDetails

import forms.mappings.{Mappings, Regex}
import models.{PersonName, SchemeManagersName}
import play.api.data.Form
import play.api.data.Forms._

import javax.inject.Inject

class SchemeManagersNameFormProvider @Inject() extends Mappings with Regex {

  def apply(): Form[PersonName] = Form(
    mapping(
      "schemeManagersFirstName" -> text("schemeManagersName.error.firstName.required")
        .verifying(maxLength(35, "schemeManagersName.error.firstName.length"))
        .verifying(regexp(nameRegex, "schemeManagersName.error.firstName.pattern")),
      "schemeManagersLastName"  -> text("schemeManagersName.error.lastName.required")
        .verifying(maxLength(35, "schemeManagersName.error.lastName.length"))
        .verifying(regexp(nameRegex, "schemeManagersName.error.lastName.pattern"))
    )(PersonName.apply)(x => Some((x.firstName, x.lastName)))
  )
}

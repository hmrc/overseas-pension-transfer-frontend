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

package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.MemberName

class MemberNameFormProvider @Inject() extends Mappings {

   def apply(): Form[MemberName] = Form(
     mapping(
      "memberFirstName" -> text("memberName.error.memberFirstName.required")
        .verifying(maxLength(35, "memberName.error.memberFirstName.length"))
        .verifying(regexp("^[A-Za-zÀ-ÖØ-öø-ÿ][A-Za-zÀ-ÖØ-öø-ÿ'-]*$", "memberName.error.memberFirstName.pattern")),
      "memberLastName" -> text("memberName.error.memberLastName.required")
        .verifying(maxLength(35, "memberName.error.memberLastName.length"))
        .verifying(regexp("^[A-Za-zÀ-ÖØ-öø-ÿ][A-Za-zÀ-ÖØ-öø-ÿ'-]*$", "memberName.error.memberFirstName.pattern")),
    )(MemberName.apply)(x => Some((x.memberFirstName, x.memberLastName)))
   )
 }

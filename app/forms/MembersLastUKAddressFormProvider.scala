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

import forms.mappings.{Mappings, Regex}
import models.address._
import play.api.data.Forms._
import play.api.data.{Form, Forms}

import javax.inject.Inject

class MembersLastUKAddressFormProvider @Inject() extends Mappings with Regex {

  private val length35 = 35
  private val length16 = 16

  def apply(memberName: String): Form[MembersLastUKAddress] = {
    Form(
      mapping(
        "addressLine1" -> text("membersLastUKAddress.error.addressLine1.required", Seq(memberName))
          .verifying(maxLength(length35, "common.addressInput.error.addressLine1.length"))
          .verifying(regexp(addressLinesRegex, "common.addressInput.error.addressLine1.pattern")),
        "addressLine2" -> text("membersLastUKAddress.error.addressLine2.required", Seq(memberName))
          .verifying(maxLength(length35, "common.addressInput.error.addressLine2.length"))
          .verifying(regexp(addressLinesRegex, "common.addressInput.error.addressLine2.pattern")),
        "addressLine3" -> optional(
          Forms.text
            verifying maxLength(length35, "common.addressInput.error.addressLine3.length")
            verifying regexp(addressLinesRegex, "common.addressInput.error.addressLine3.pattern")
        ),
        "addressLine4" -> optional(
          Forms.text
            verifying maxLength(length35, "common.addressInput.error.addressLine4.length")
            verifying regexp(addressLinesRegex, "common.addressInput.error.addressLine4.pattern")
        ),
        "postcode"     -> text("membersLastUKAddress.error.postcode.required")
          .verifying(maxLength(length16, "membersLastUKAddress.error.postcode.length"))
          .verifying(regexp(postcodeRegex, "membersLastUKAddress.error.postcode.incorrect"))
      )(MembersLastUKAddress.apply)(MembersLastUKAddress.unapply)
    )
  }
}

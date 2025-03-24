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
import models.UserAnswers
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
          .verifying(maxLength(length35, "membersLastUKAddress.error.addressLine1.length"))
          .verifying(regexp(addressLinesRegex, "membersLastUKAddress.error.addressLine1.pattern")),
        "addressLine2" -> optional(
          Forms.text
            verifying maxLength(length35, "membersLastUKAddress.error.addressLine2.length")
            verifying regexp(addressLinesRegex, "membersLastUKAddress.error.addressLine2.pattern")
        ),
        "townOrCity"   -> text("membersLastUKAddress.error.city.required", Seq(memberName))
          .verifying(maxLength(length35, "membersLastUKAddress.error.city.length"))
          .verifying(regexp(addressLinesRegex, "membersLastUKAddress.error.city.pattern")),
        "county"       -> optional(
          Forms.text
            verifying maxLength(length35, "membersLastUKAddress.error.county.length")
            verifying regexp(addressLinesRegex, "membersLastUKAddress.error.county.pattern")
        ),
        "postcode"     -> text("membersLastUKAddress.error.postcode.required")
          .verifying(maxLength(length16, "membersLastUKAddress.error.postcode.length"))
          .verifying(regexp(postcodeRegex, "membersLastUKAddress.error.postcode.incorrect"))
      )(MembersLastUKAddress.apply)(MembersLastUKAddress.unapply)
    )
  }
}

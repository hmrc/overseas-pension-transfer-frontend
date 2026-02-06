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

import forms.mappings.{Mappings, Regex}
import models.address._
import models.requests.DisplayRequest
import play.api.data.Forms._
import play.api.data.{Form, Forms}
import utils.AppUtils

import javax.inject.Inject

class MembersLastUKAddressNewFormProvider @Inject() extends Mappings with Regex with AppUtils {

  private val length35 = 35
  private val length16 = 16

  def apply()(implicit request: DisplayRequest[_]): Form[MembersLastUKAddress] = {
    val memberName = request.memberName
    Form(
      mapping(
        "addressLine1" -> text("membersLastUKAddress.error.addressLine1.required", Seq(memberName))
          .transform[String](input => input.trim, identity)
          .verifying(maxLength(length35, "common.addressInput.error.addressLine1.length"))
          .verifying(regexp(addressLinesRegex, "common.addressInput.error.addressLine1.pattern")),
        "addressLine2" -> text("membersLastUKAddress.error.addressLine2.required", Seq(memberName))
          .transform[String](input => input.trim, identity)
          .verifying(maxLength(length35, "common.addressInput.error.addressLine2.length"))
          .verifying(regexp(addressLinesRegex, "common.addressInput.error.addressLine2.pattern")),
        "townOrCity"   -> optional(
          Forms.text
            .transform[String](input => input.trim, identity)
            .verifying(maxLength(length35, "common.addressInput.error.townOrCity.length"))
            .verifying(regexp(addressLinesRegex, "common.addressInput.error.townOrCity.pattern"))
        ),
        "county"       -> optional(
          Forms.text
            .transform[String](input => input.trim, identity)
            .verifying(maxLength(length35, "common.addressInput.error.county.length"))
            .verifying(regexp(addressLinesRegex, "common.addressInput.error.county.pattern"))
        ),
        "postcode"     -> text("membersLastUKAddress.error.postcode.required")
          .transform[String](
            raw => formatUkPostcode(raw),
            formatted => formatted
          )
          .verifying(maxLength(length16, "membersLastUKAddress.error.postcode.length"))
          .verifying(
            "membersLastUKAddress.error.postcode.incorrect",
            { postcode =>
              val parts = postcode.split("\\s+")
              if (parts.length == 2) {
                val outcode = parts(0)
                val incode  = parts(1)
                (outcode.matches(postcodeOutcodeRegex) && incode.matches(postcodeIncodeRegex)) ||
                postcode == "GIR 0AA"
              } else {
                false
              }
            }
          )
      )(MembersLastUKAddress.apply)(MembersLastUKAddress.unapply)
    )
  }
}

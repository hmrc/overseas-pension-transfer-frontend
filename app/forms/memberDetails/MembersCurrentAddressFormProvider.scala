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

import javax.inject.Inject
import forms.mappings.{Mappings, Regex}
import play.api.data.{Form, Forms}
import play.api.data.Forms._
import models.address._
import models.requests.DisplayRequest

case class MembersCurrentAddressFormData(
    addressLine1: String,
    addressLine2: String,
    addressLine3: Option[String],
    addressLine4: Option[String],
    countryCode: String,
    postcode: Option[String],
    poBox: Option[String]
  )

object MembersCurrentAddressFormData {

  def fromDomain(address: MembersCurrentAddress): MembersCurrentAddressFormData =
    MembersCurrentAddressFormData(
      addressLine1 = address.addressLine1,
      addressLine2 = address.addressLine2,
      addressLine3 = address.addressLine3,
      addressLine4 = address.addressLine4,
      countryCode  = address.country.code,
      postcode     = address.postcode,
      poBox        = address.poBox
    )
}

class MembersCurrentAddressFormProvider @Inject() extends Mappings with Regex {

  def apply()(implicit request: DisplayRequest[_]): Form[MembersCurrentAddressFormData] = {
    val memberName = request.memberName
    Form(
      mapping(
        "addressLine1" -> text("membersCurrentAddress.error.addressLine1.required", Seq(memberName))
          .verifying(maxLength(35, "common.addressInput.error.addressLine1.length"))
          .verifying(regexp(addressLinesRegex, "common.addressInput.error.addressLine1.pattern")),
        "addressLine2" -> text("membersCurrentAddress.error.addressLine2.required", Seq(memberName))
          .verifying(maxLength(35, "common.addressInput.error.addressLine2.length"))
          .verifying(regexp(addressLinesRegex, "common.addressInput.error.addressLine2.pattern")),
        "addressLine3" -> optional(
          Forms.text
            verifying maxLength(35, "common.addressInput.error.addressLine3.length")
            verifying regexp(addressLinesRegex, "common.addressInput.error.addressLine3.pattern")
        ),
        "addressLine4" -> optional(
          Forms.text
            verifying maxLength(35, "common.addressInput.error.addressLine4.length")
            verifying regexp(addressLinesRegex, "common.addressInput.error.addressLine4.pattern")
        ),
        "countryCode"  -> text("common.addressInput.error.countryCode.required"),
        "postcode"     -> optional(
          Forms.text
            verifying maxLength(35, "common.addressInput.error.postcode.length")
            verifying regexp(internationalPostcodeRegex, "common.addressInput.error.postcode.pattern")
        ),
        "poBox"        -> optional(
          Forms.text
            verifying maxLength(35, "common.addressInput.error.poBox.length")
            verifying regexp(poBoxRegex, "common.addressInput.error.poBox.pattern")
        )
      )(MembersCurrentAddressFormData.apply)(MembersCurrentAddressFormData.unapply)
    )
  }
}

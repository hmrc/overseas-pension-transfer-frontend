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
import forms.mappings.{Mappings, Regex}
import play.api.data.{Form, Forms}
import play.api.data.Forms._
import models.address._

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

  def apply(memberName: String): Form[MembersCurrentAddressFormData] = Form(
    mapping(
      "addressLine1" -> text("membersCurrentAddress.error.addressLine1.required", Seq(memberName))
        .verifying(maxLength(35, "membersCurrentAddress.error.addressLine1.length"))
        .verifying(regexp(addressLinesRegex, "membersCurrentAddress.error.addressLine1.pattern")),
      "addressLine2" -> text("membersCurrentAddress.error.addressLine2.required", Seq(memberName))
        .verifying(maxLength(35, "membersCurrentAddress.error.addressLine2.length"))
        .verifying(regexp(addressLinesRegex, "membersCurrentAddress.error.addressLine2.pattern")),
      "addressLine3" -> optional(
        Forms.text
          verifying maxLength(35, "membersCurrentAddress.error.addressLine3.length")
          verifying regexp(addressLinesRegex, "membersCurrentAddress.error.addressLine3.pattern")
      ),
      "addressLine4" -> optional(
        Forms.text
          verifying maxLength(35, "membersCurrentAddress.error.addressLine4.length")
          verifying regexp(addressLinesRegex, "membersCurrentAddress.error.addressLine4.pattern")
      ),
      "countryCode"  -> text("membersCurrentAddress.error.countryCode.required"),
      "postcode"     -> optional(
        Forms.text
          verifying maxLength(35, "membersCurrentAddress.error.postcode.length")
          verifying regexp(internationalPostcodeRegex, "membersCurrentAddress.error.postcode.pattern")
      ),
      "poBox"        -> optional(
        Forms.text
          verifying maxLength(35, "membersCurrentAddress.error.poBox.length")
          verifying regexp(poBoxRegex, "membersCurrentAddress.error.poBox.pattern")
      )
    )(MembersCurrentAddressFormData.apply)(MembersCurrentAddressFormData.unapply)
  )
}

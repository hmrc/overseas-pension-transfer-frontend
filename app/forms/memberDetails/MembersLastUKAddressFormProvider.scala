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

import javax.inject.Inject

case class MembersLastUKAddressFormData(
    addressLine1: String,
    addressLine2: String,
    addressLine3: Option[String],
    addressLine4: Option[String],
    postcode: String
  )

object MembersLastUKAddressFormData {

  def toDomain(formData: MembersLastUKAddressFormData): MembersLastUKAddress =
    MembersLastUKAddress(
      BaseAddress(
        line1    = formData.addressLine1,
        line2    = formData.addressLine2,
        line3    = formData.addressLine3,
        line4    = formData.addressLine4,
        line5    = None,
        country  = Countries.UK,
        postcode = Some(formData.postcode),
        poBox    = None
      )
    )

  def fromDomain(address: MembersLastUKAddress): MembersLastUKAddressFormData =
    MembersLastUKAddressFormData(
      addressLine1 = address.base.line1,
      addressLine2 = address.base.line2,
      addressLine3 = address.base.line3,
      addressLine4 = address.base.line4,
      postcode     = address.base.postcode.getOrElse("")
    )
}

class MembersLastUKAddressFormProvider @Inject() extends Mappings with Regex {

  private val length35 = 35
  private val length16 = 16

  def apply()(implicit request: DisplayRequest[_]): Form[MembersLastUKAddressFormData] = {
    val memberName = request.memberName
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
      )(MembersLastUKAddressFormData.apply)(MembersLastUKAddressFormData.unapply)
    )
  }
}

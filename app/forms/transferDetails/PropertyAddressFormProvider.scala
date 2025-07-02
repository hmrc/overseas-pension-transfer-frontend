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
import models.address._
import models.requests.DisplayRequest
import play.api.data.Forms._
import play.api.data.{Form, Forms}

import javax.inject.Inject

case class PropertyAddressFormData(
    addressLine1: String,
    addressLine2: String,
    addressLine3: Option[String],
    addressLine4: Option[String],
    countryCode: String,
    postcode: Option[String]
  )

object PropertyAddressFormData {

  def fromDomain(address: PropertyAddress): PropertyAddressFormData =
    PropertyAddressFormData(
      addressLine1 = address.base.line1,
      addressLine2 = address.base.line2,
      addressLine3 = address.base.line3,
      addressLine4 = address.base.line4,
      countryCode  = address.base.country.code,
      postcode     = address.base.postcode
    )
}

class PropertyAddressFormProvider @Inject() extends Mappings with Regex {

  def apply()(implicit request: DisplayRequest[_]): Form[PropertyAddressFormData] = {
    Form(
      mapping(
        "addressLine1" -> text("propertyAddress.error.addressLine1.required")
          .verifying(maxLength(35, "common.addressInput.error.addressLine1.length"))
          .verifying(regexp(addressLinesRegex, "common.addressInput.error.addressLine1.pattern")),
        "addressLine2" -> text("propertyAddress.error.addressLine2.required")
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
        )
      )(PropertyAddressFormData.apply)(PropertyAddressFormData.unapply)
    )
  }
}

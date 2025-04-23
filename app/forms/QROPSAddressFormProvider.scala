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
import models.address.QROPSAddress
import play.api.data.{Form, Forms}
import play.api.data.Forms._

case class QROPSAddressFormData(
    addressLine1: String,
    addressLine2: String,
    addressLine3: Option[String],
    addressLine4: Option[String],
    addressLine5: Option[String],
    countryCode: String
  )

object QROPSAddressFormData {

  def fromDomain(address: QROPSAddress): QROPSAddressFormData =
    QROPSAddressFormData(
      addressLine1 = address.addressLine1,
      addressLine2 = address.addressLine2,
      addressLine3 = address.addressLine3,
      addressLine4 = address.addressLine4,
      addressLine5 = address.addressLine5,
      countryCode  = address.country.code
    )
}

class QROPSAddressFormProvider @Inject() extends Mappings with Regex {

  def apply(): Form[QROPSAddressFormData] = Form(
    mapping(
      "addressLine1" -> text("qROPSAddress.error.addressLine1.required")
        .verifying(maxLength(35, "common.addressInput.error.addressLine1.length"))
        .verifying(regexp(addressLinesRegex, "common.addressInput.error.addressLine1.pattern")),
      "addressLine2" -> text("qROPSAddress.error.addressLine2.required")
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
      "addressLine5" -> optional(
        Forms.text
          verifying maxLength(35, "common.addressInput.error.addressLine5.length")
          verifying regexp(addressLinesRegex, "common.addressInput.error.addressLine5.pattern")
      ),
      "countryCode"  -> text("common.addressInput.error.countryCode.required")
    )(QROPSAddressFormData.apply)(QROPSAddressFormData.unapply)
  )

}

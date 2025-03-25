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

class MembersCurrentAddressFormProvider @Inject() extends Mappings with Regex {

  def apply(): Form[MembersCurrentAddress] = Form(
    mapping(
      "addressLine1" -> text("membersCurrentAddress.error.addressLine1.required")
        .verifying(maxLength(35, "membersCurrentAddress.error.addressLine1.length"))
        .verifying(regexp(addressLinesRegex, "membersCurrentAddress.error.addressLine1.pattern")),
      "addressLine2" -> text("membersCurrentAddress.error.addressLine2.required")
        .verifying(maxLength(35, "membersCurrentAddress.error.addressLine2.length"))
        .verifying(regexp(addressLinesRegex, "membersCurrentAddress.error.addressLine2.pattern")),
      "addressLine3" -> optional(
        Forms.text
          verifying maxLength(35, "membersCurrentAddress.error.addressLine3.length")
          verifying regexp(addressLinesRegex, "membersCurrentAddress.error.addressLine3.pattern")
      ),
      "city"         -> optional(
        Forms.text
          verifying maxLength(35, "membersCurrentAddress.error.city.length")
          verifying regexp(addressLinesRegex, "membersCurrentAddress.error.city.pattern")
      ),
      "country"      -> text("membersCurrentAddress.error.addressLine2.required")
        .verifying(maxLength(35, "membersCurrentAddress.error.addressLine2.length")),
      "postcode"     -> optional(
        Forms.text
          verifying maxLength(16, "membersCurrentAddress.error.postcode.length")
      )
    )(MembersCurrentAddress.apply)(MembersCurrentAddress.unapply)
  )
}

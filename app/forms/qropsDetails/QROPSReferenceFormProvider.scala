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

package forms.qropsDetails

import javax.inject.Inject
import forms.mappings.{Mappings, Regex}
import play.api.data.Form

class QROPSReferenceFormProvider @Inject() extends Mappings with Regex {

  val referencePrefix = "QROPS"

  def apply(): Form[String] =
    Form(
      "qropsRef" -> text("qropsReference.error.required")
        .verifying(regexp(qropsRefRegex, "qropsReference.error.invalid"))
        .transform[String](
          raw => prependReferencePrefix(raw),
          formatted => formatted
        )
    )

  private def prependReferencePrefix(raw: String): String = {
    if (raw.startsWith(referencePrefix)) raw else s"$referencePrefix$raw"
  }
}

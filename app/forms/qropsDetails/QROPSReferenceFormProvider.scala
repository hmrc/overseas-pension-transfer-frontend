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

import forms.mappings.{Mappings, Regex}
import play.api.data.Form

import javax.inject.Inject

class QROPSReferenceFormProvider @Inject() extends Mappings with Regex {

  val referencePrefix = "QROPS"

  def apply(): Form[String] =
    Form(
      "qropsRef" -> text("qropsReference.error.required")
        .transform[String](
          raw => formatQropsRef(raw),
          identity
        )
        .verifying(regexp(qropsRefRegex, "qropsReference.error.invalid"))
    )

  private def formatQropsRef(raw: String): String = {
    val fd = raw.replaceAll("\\s+", "").toUpperCase
    if (fd.startsWith(referencePrefix)) fd else s"$referencePrefix$fd"
  }
}

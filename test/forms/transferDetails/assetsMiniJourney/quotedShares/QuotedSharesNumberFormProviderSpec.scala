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

package forms.transferDetails.assetsMiniJourney.quotedShares

import forms.behaviours.StringFieldBehaviours
import forms.mappings.Regex
import org.scalacheck.Gen
import play.api.data.FormError

class QuotedSharesNumberFormProviderSpec extends StringFieldBehaviours with Regex {

  val form = new QuotedSharesNumberFormProvider()()

  ".value" - {

    val fieldName = "value"

    val validNumberOfShares: Gen[String] =
      for {
        wholePart  <- Gen.choose(1, 999999)
        decimalOpt <- Gen.option(Gen.choose(1, 99))
      } yield decimalOpt match {
        case Some(decimal) => s"$wholePart.${decimal.toString}"
        case None          => wholePart.toString
      }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "quotedSharesNumber.error.required")
    )

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validNumberOfShares
    )

    "fail to bind non-numeric value" in {
      val result = form.bind(Map(fieldName -> "abc"))
      result.errors must contain only FormError(fieldName, "quotedSharesNumber.error.invalid")
    }

    "fail to bind negative number" in {
      val result = form.bind(Map(fieldName -> "-50"))
      result.errors must contain only FormError(fieldName, "quotedSharesNumber.error.negative")
    }

    "fail to bind zero" in {
      val result = form.bind(Map(fieldName -> "0"))
      result.errors must contain only FormError(fieldName, "quotedSharesNumber.error.invalid")
    }

    "fail to bind multiple decimal points" in {
      val result = form.bind(Map(fieldName -> "1.2.3"))
      result.errors must contain only FormError(fieldName, "quotedSharesNumber.error.invalid")
    }

    "fail to bind number with trailing dot" in {
      val result = form.bind(Map(fieldName -> "10."))
      result.errors must contain only FormError(fieldName, "quotedSharesNumber.error.invalid")
    }
  }
}

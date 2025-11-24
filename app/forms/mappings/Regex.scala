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

package forms.mappings

trait Regex {

  val nameRegex: String = "^[A-Za-zÀ-ÖØ-öø-ÿ]+(?:[ '-][A-Za-zÀ-ÖØ-öø-ÿ]+)*$"

  val ninoRegex: String = "^[A-Za-z]{2}\\d{6}[A-Za-z]$"

  val addressLinesRegex: String = "^[a-zA-ZÀ-ÖØ-öø-ÿ0-9\\s\\-,.&'\\/]+$"

  val postcodeRegex: String = "^(GIR0AA|[A-Za-z]{1,2}[0-9][0-9A-Za-z]? ?[0-9][A-Za-z]{2})$"

  val internationalPostcodeRegex: String = "^[A-Za-z0-9\\s]+$"

  val poBoxRegex: String = "^[A-Za-z0-9\\s]+$"

  val qropsRefRegex: String = "^(QROPS\\d{6}|QROPS|\\d{6})$"

  val phoneNumberRegex: String = "^\\+?[0-9]+$"

  val descriptionRegex: String = """^[A-Za-z0-9 \-,.&'/]+$"""

  val classRegex: String = "^[A-Za-zÀ-ÖØ-öø-ÿ]+(?:[ '-][A-Za-zÀ-ÖØ-öø-ÿ]+)*$"

  val psaIdRegex: String = "^[A-Za-z][0-9]{7}$"

}

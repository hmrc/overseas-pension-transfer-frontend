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

package models

import play.api.libs.json._

sealed trait AssetEntry

case class SharesEntry(
    companyName: String,
    valueOfShares: BigDecimal,
    numberOfShares: String,
    classOfShares: String
  ) extends AssetEntry

object SharesEntry {
  val CompanyName    = "companyName"
  val ValueOfShares  = "valueOfShares"
  val NumberOfShares = "numberOfShares"
  val ClassOfShares  = "classOfShares"

  implicit val format: OFormat[SharesEntry] = Json.format[SharesEntry]
}

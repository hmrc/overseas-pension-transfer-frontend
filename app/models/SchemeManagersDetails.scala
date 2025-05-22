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

import models.address.Address
import play.api.libs.json.{Json, OFormat}

case class SchemeManagersDetails(
    qropsSchemeManagerType: String,
    schemeManagersName: Option[String],
    organisationName: Option[String],
    orgIndividualName: Option[String],
    schemeManagersAddress: Address,
    schemeManagerEmail: String,
    schemeManagersContact: String
  )

object SchemeManagersDetails {
  implicit val format: OFormat[SchemeManagersDetails] = Json.format[SchemeManagersDetails]
}

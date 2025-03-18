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

trait Address {
  val line1: String
  val line2: Option[String]
  val line3: Option[String]
  val townOrCity: Option[String]
  /*TODO
     Once we implement the country look up on the manual entry page, the country should be changed to the country object itself
     which contains a country code and country name (see the country model)
   */
  val country: Option[String]
  val postcode: Option[String]
}

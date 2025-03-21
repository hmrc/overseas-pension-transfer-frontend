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

package viewmodels

trait AddressField[A] {
  def toOption(a: A): Option[String]
}

object AddressField {

  implicit val stringField: AddressField[String] = (a: String) => if (a.nonEmpty) Some(a) else None

  implicit val optionStringField: AddressField[Option[String]] = (a: Option[String]) => a.filter(_.nonEmpty)

}

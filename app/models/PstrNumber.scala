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
import play.api.mvc.QueryStringBindable

case class PstrNumber(value: String)

object PstrNumber {
  implicit val format: Format[PstrNumber] = Json.valueFormat[PstrNumber]

  implicit val queryBindable: QueryStringBindable[PstrNumber] = new QueryStringBindable[PstrNumber] {

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, PstrNumber]] =
      params.get(key).flatMap(_.headOption).map { raw =>
        val norm = raw.trim.toUpperCase
        Right(PstrNumber(norm))
      }

    override def unbind(key: String, p: PstrNumber): String =
      s"$key=${p.value}"
  }

}

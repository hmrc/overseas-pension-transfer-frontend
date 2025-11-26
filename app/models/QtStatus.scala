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

import play.api.mvc.QueryStringBindable

sealed trait QtStatus

object QtStatus extends Enumerable.Implicits {

  case object Compiled        extends WithName("Compiled") with QtStatus
  case object Submitted       extends WithName("Submitted") with QtStatus
  case object InProgress      extends WithName("InProgress") with QtStatus
  case object AmendInProgress extends WithName("AmendInProgress") with QtStatus

  val values: Seq[QtStatus] = Seq(
    Compiled,
    Submitted,
    InProgress,
    AmendInProgress
  )

  implicit val enumerable: Enumerable[QtStatus] =
    Enumerable(values.map(v => v.toString -> v): _*)

  def parse(s: String): Option[QtStatus] =
    values.find(_.toString.equalsIgnoreCase(s.trim))

  implicit val queryBindable: QueryStringBindable[QtStatus] =
    new QueryStringBindable[QtStatus] {

      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, QtStatus]] =
        params.get(key).flatMap(_.headOption).map { raw =>
          parse(raw).toRight(s"Invalid QtStatus for '$key': '$raw'")
        }

      override def unbind(key: String, value: QtStatus): String =
        s"$key=${value.toString}"
    }
}

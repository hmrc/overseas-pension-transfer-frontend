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

import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

trait TransferId {
  def value: String
}

object TransferId {

  def apply(value: String): TransferId =
    (QtNumber.from(value), TransferNumber.from(value)) match {
      case (Right(qtNumber), Left(_))                       => qtNumber
      case (Left(_), Right(transferNumber: TransferNumber)) => transferNumber
    }

  implicit val reads: Reads[TransferId] = Reads {
    case JsString(value) => JsSuccess(apply(value))
    case _               => JsError("Unable to parse as TransferId")
  }

  implicit val writes: Writes[TransferId] = Writes {
    transferId =>
      JsString(transferId.value)
  }

  implicit val format: Format[TransferId] = Format(reads, writes)

  implicit val queryBindable: QueryStringBindable[TransferId] = new QueryStringBindable[TransferId] {

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, TransferId]] =
      params.get(key).flatMap(_.headOption).map { raw =>
        val norm = raw.trim
        Try(QtNumber(norm)) match {
          case Success(qtNumber) => Right(qtNumber)
          case _                 =>
            Try(TransferNumber(norm)) match {
              case Success(transferNumber) => Right(transferNumber)
              case Failure(_)              => Left("Invalid TransferId")
            }
        }
      }

    override def unbind(key: String, p: TransferId): String =
      s"$key=${p.value}"
  }
}

case class QtNumber(value: String) extends TransferId {
  def isEmpty: Boolean  = value.trim.isEmpty
  def nonEmpty: Boolean = !isEmpty
}

object QtNumber {
  val empty: QtNumber = QtNumber("")
  val regex: Regex    = "QT[0-9]{6}".r

  def from(s: String): Either[String, QtNumber] =
    if (regex.matches(s)) {
      Right(QtNumber(s))
    } else {
      Left("QTNumber must be QT followed by 6 digits e.g. QT123456")
    }

  /** Validates using normalised input, but keeps the original string exactly as provided. */
  implicit val reads: Reads[QtNumber] =
    __.read[String]
      .filter(JsonValidationError("qtNumber.invalid"))(s => regex.matches(s))
      .map(QtNumber(_))

  /** Writes the value exactly as is (no normalise). */
  implicit val writes: Writes[QtNumber] =
    Writes[QtNumber](p => JsString(p.value))

  implicit val format: Format[QtNumber] = Format(reads, writes)

}

case class TransferNumber(value: String) extends TransferId

object TransferNumber {
  val regex: Regex = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$".r

  def from(s: String): Either[String, TransferNumber] =
    if (regex.matches(s)) {
      Right(TransferNumber(s))
    } else {
      Left("transferNumber must be UUID format")
    }

  /** Validates using normalised input, but keeps the original string exactly as provided. */
  implicit val reads: Reads[TransferNumber] =
    __.read[String]
      .filter(JsonValidationError("transferNumber.invalid"))(s => regex.matches(s))
      .map(TransferNumber(_))

  /** Writes the value exactly as is (no normalise). */
  implicit val writes: Writes[TransferNumber] =
    Writes[TransferNumber](p => JsString(p.value))

  implicit val format: Format[TransferNumber] = Format(reads, writes)
}

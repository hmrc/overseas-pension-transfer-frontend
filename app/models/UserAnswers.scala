/*
 * Copyright 2024 HM Revenue & Customs
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

import models.TaskCategory._
import models.taskList.TaskStatus._
import play.api.libs.json._
import queries.{Gettable, Settable, TaskStatusQuery}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant
import scala.util.{Failure, Success, Try}

class DeserialisationException(message: String) extends RuntimeException(message)

final case class UserAnswers(
    id: String,
    pstr: PstrNumber,
    data: JsObject       = Json.obj(),
    lastUpdated: Instant = Instant.now
  ) {

  def get[A](page: Gettable[A])(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(page.path)).reads(data).getOrElse(None)

  import play.api.libs.json._

  def getWithLogging[A](page: Gettable[A])(implicit rds: Reads[A], mf: Manifest[A]): Either[Throwable, A] = {
    val path     = page.path
    val rawValue = path.asSingleJson(data).getOrElse(JsNull)
    val result   = Reads.at(path).reads(data)

    result match {
      case JsSuccess(value, _) =>
        Right(value)

      case JsError(errors) =>
        val errorMsg =
          s"""
             |Path     : $path
             |Expected : ${mf.runtimeClass.getSimpleName}
             |Actual   : ${Json.prettyPrint(rawValue)}
             |Errors   : ${errors.map {
              case (jsPath, validationErrors) =>
                s"$jsPath -> ${validationErrors.map(_.message).mkString(", ")}"
            }.mkString("\n           |           ")}
             |""".stripMargin

        Left(new DeserialisationException(errorMsg))
    }
  }

  def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = {

    val updatedData = data.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(errors)       =>
        Failure(JsResultException(errors))
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy(data = d)
        page.cleanup(Some(value), updatedAnswers)
    }
  }

  def remove[A](page: Settable[A]): Try[UserAnswers] = {

    val updatedData = data.removeObject(page.path) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(_)            =>
        Success(data)
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy(data = d)
        page.cleanup(None, updatedAnswers)
    }
  }
}

object UserAnswers {

  def buildMinimal[A](
      original: UserAnswers,
      page: Settable[A] with Gettable[A]
    )(implicit reads: Reads[A],
      writes: Writes[A],
      mf: Manifest[A]
    ): Try[UserAnswers] =
    original.getWithLogging(page) match {
      case Right(value) =>
        original.copy(data = Json.obj()).set(page, value)
      case Left(error)  =>
        Failure(error)
    }

  val reads: Reads[UserAnswers] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "_id").read[String] and
        (__ \ "pstr").read[String].map(PstrNumber.apply) and
        (__ \ "data").read[JsObject] and
        (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat)
    )(UserAnswers.apply _)
  }

  val writes: OWrites[UserAnswers] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "_id").write[String] and
        (__ \ "pstr").write[PstrNumber] and
        (__ \ "data").write[JsObject] and
        (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat)
    )(ua => (ua.id, ua.pstr, ua.data, ua.lastUpdated))
  }

  implicit val format: OFormat[UserAnswers] = OFormat(reads, writes)
}

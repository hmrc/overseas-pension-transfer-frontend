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

import models.TaskCategory.{MemberDetails, QROPSDetails, SchemeManagerDetails, SubmissionDetails, TransferDetails}
import models.authentication.AuthenticatedUser
import models.taskList.TaskStatus.{CannotStart, NotStarted}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import queries.{Gettable, Settable, TaskStatusQuery}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import uk.gov.hmrc.play.audit.model.TransactionFailure

import java.time.Instant
import scala.util.{Failure, Success, Try}

case class SessionData(
    sessionId: String,
    transferId: TransferId,
    schemeInformation: PensionSchemeDetails,
    user: AuthenticatedUser,
    data: JsObject,
    lastUpdated: Instant = Instant.now
  ) {

  def get[A](page: Gettable[A])(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(page.path)).reads(data).getOrElse(None)

  import play.api.libs.json._

  def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[SessionData] = {

    val updatedData = data.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(errors)       =>
        Failure(JsResultException(errors))
    }

    updatedData.flatMap {
      d =>
        val updatedSession = copy(data = d)
        Success(updatedSession)
    }
  }

  def remove[A](page: Settable[A]): Try[SessionData] = {

    val updatedData = data.removeObject(page.path) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(_)            =>
        Success(data)
    }

    updatedData.flatMap {
      d =>
        val updatedSession = copy(data = d)
        Success(updatedSession)
    }
  }
}

object SessionData {

  implicit val reads: Reads[SessionData] = (
    (__ \ "_id").read[String] and
      (__ \ "transferId").read[TransferId] and
      (__ \ "schemeInformation").read[PensionSchemeDetails] and
      (__ \ "user").read[AuthenticatedUser] and
      (__ \ "data").read[JsObject] and
      (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat)
  )(SessionData.apply _)

  implicit val writes: Writes[SessionData] = (
    (__ \ "_id").write[String] and
      (__ \ "transferId").write[TransferId] and
      (__ \ "schemeInformation").write[PensionSchemeDetails] and
      (__ \ "user").write[AuthenticatedUser] and
      (__ \ "data").write[JsObject] and
      (__ \ "lastUpdated").write(MongoJavatimeFormats.instantWrites)
  )(session => (session.sessionId, session.transferId, session.schemeInformation, session.user, session.data, session.lastUpdated))

  implicit val format: Format[SessionData] = Format[SessionData](reads, writes)

  def initialise(sd: SessionData): Try[SessionData] =
    for {
      sd1 <- sd.set(TaskStatusQuery(MemberDetails), NotStarted)
      sd2 <- sd1.set(TaskStatusQuery(QROPSDetails), CannotStart)
      sd3 <- sd2.set(TaskStatusQuery(SchemeManagerDetails), CannotStart)
      sd4 <- sd3.set(TaskStatusQuery(TransferDetails), CannotStart)
      sd5 <- sd4.set(TaskStatusQuery(SubmissionDetails), CannotStart)
    } yield sd5
}

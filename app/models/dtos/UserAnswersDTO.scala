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

package models.dtos

import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import java.time.Instant
import models.UserAnswers
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}

final case class UserAnswersDTO(
    id: String,
    data: JsObject,
    lastUpdated: Instant
  )

object UserAnswersDTO {

  implicit val format: OFormat[UserAnswersDTO] = {
    val reads: Reads[UserAnswersDTO] = (
      (__ \ "id").read[String] and
        (__ \ "data").read[JsObject] and
        (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat)
    )(UserAnswersDTO.apply _)

    val writes: OWrites[UserAnswersDTO] = (
      (__ \ "id").write[String] and
        (__ \ "data").write[JsObject] and
        (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat)
    )(unlift(UserAnswersDTO.unapply))

    OFormat(reads, writes)
  }

  def fromUserAnswers(ua: UserAnswers): UserAnswersDTO =
    UserAnswersDTO(
      id          = ua.id,
      data        = ua.data,
      lastUpdated = ua.lastUpdated
    )

  def toUserAnswers(dto: UserAnswersDTO): UserAnswers =
    UserAnswers(
      id          = dto.id,
      data        = dto.data,
      lastUpdated = dto.lastUpdated
    )
}

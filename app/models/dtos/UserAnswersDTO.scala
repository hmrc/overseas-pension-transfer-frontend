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

import models.{PstrNumber, UserAnswers}
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json._

import java.time.Instant

final case class UserAnswersDTO(
    referenceId: String,
    pstr: PstrNumber,
    data: JsObject,
    lastUpdated: Instant
  )

object UserAnswersDTO {

  implicit val format: OFormat[UserAnswersDTO] = {
    val reads: Reads[UserAnswersDTO] = (
      (__ \ "referenceId").read[String] and
        (__ \ "pstr").read[String].map(PstrNumber.apply) and
        (__ \ "data").read[JsObject] and
        (__ \ "lastUpdated").read[Instant]
    )(UserAnswersDTO.apply _)

    val writes: OWrites[UserAnswersDTO] = (
      (__ \ "referenceId").write[String] and
        (__ \ "pstr").write[PstrNumber] and
        (__ \ "data").write[JsObject] and
        (__ \ "lastUpdated").write[Instant]
    )(uad => (uad.referenceId, uad.pstr, uad.data, uad.lastUpdated))

    OFormat(reads, writes)
  }

  def fromUserAnswers(ua: UserAnswers): UserAnswersDTO =
    UserAnswersDTO(
      // The reference id WILL NOT be the user answers id. I only put this here because
      // the actual implementation is outside the scope of my ticket.
      referenceId = ua.id,
      pstr        = ua.pstr,
      data        = ua.data,
      lastUpdated = ua.lastUpdated
    )

  def toUserAnswers(dto: UserAnswersDTO): UserAnswers =
    UserAnswers(
      id          = dto.referenceId,
      pstr        = dto.pstr,
      data        = dto.data,
      lastUpdated = dto.lastUpdated
    )
}

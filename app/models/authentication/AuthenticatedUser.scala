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

package models.authentication

import models.PensionSchemeDetails
import uk.gov.hmrc.auth.core.AffinityGroup
import play.api.libs.json._

sealed trait AuthenticatedUser {
  def internalId: String
  def userType: UserType
}

object AuthenticatedUser {

  implicit val format: Format[AuthenticatedUser] = new Format[AuthenticatedUser] {

    override def reads(json: JsValue): JsResult[AuthenticatedUser] =
      (json.validate[PsaUser].isSuccess, json.validate[PspUser].isSuccess) match {
        case (true, false) => PsaUser.format.reads(json)
        case (false, true) => PspUser.format.reads(json)
        case _             => JsError("Json not a valid AuthenticatedUser")
      }

    override def writes(o: AuthenticatedUser): JsValue = o match {
      case psa: PsaUser => PsaUser.format.writes(psa)
      case psp: PspUser => PspUser.format.writes(psp)
    }
  }
}

case class PsaUser(
    psaId: PsaId,
    internalId: String,
    affinityGroup: AffinityGroup
  ) extends AuthenticatedUser {
  override val userType: UserType = Psa
}

object PsaUser {
  implicit val format: Format[PsaUser] = Json.format[PsaUser]
}

case class PspUser(
    pspId: PspId,
    internalId: String,
    affinityGroup: AffinityGroup
  ) extends AuthenticatedUser {
  override val userType: UserType = Psp
}

object PspUser {
  implicit val format: Format[PspUser] = Json.format[PspUser]
}

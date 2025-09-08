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

import models.SrnNumber

//Add Scheme Details in here - Scheme Name and SchemeId/SRN
sealed trait AuthenticatedUser {
  def internalId: String
  def userType: UserType
  def srn: Option[SrnNumber]

  def updateSrnNumber(srnNumber: SrnNumber): AuthenticatedUser
}

case class PsaUser(psaId: PsaId, internalId: String, srn: Option[SrnNumber] = None) extends AuthenticatedUser {
  override val userType: UserType = Psa

  override def updateSrnNumber(srnNumber: SrnNumber): AuthenticatedUser = this.copy(srn = Some(srnNumber))
}

case class PspUser(pspId: PspId, internalId: String, srn: Option[SrnNumber] = None) extends AuthenticatedUser {
  override val userType: UserType = Psp

  override def updateSrnNumber(srnNumber: SrnNumber): AuthenticatedUser = this.copy(srn = Some(srnNumber))
}

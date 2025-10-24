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

package validators

import cats.implicits.catsSyntaxValidatedIdBinCompat0
import models.{DataMissingError, GenericError, PersonName, SchemeManagerType, UserAnswers, ValidationResult}
import models.transferJourneys.SchemeManagerDetails
import pages.memberDetails.{MemberHasEverBeenResidentUKPage, MembersLastUKAddressPage}
import pages.qropsSchemeManagerDetails._

object SchemeManagerDetailsValidator extends Validator[SchemeManagerDetails] {

  override def fromUserAnswers(user: UserAnswers): ValidationResult[SchemeManagerDetails] = ???

  def validateSchemeManagerType(answers: UserAnswers): ValidationResult[SchemeManagerType] =
    answers.get(SchemeManagerTypePage) match {
      case Some(smType) => smType.validNec
      case None         => DataMissingError(SchemeManagerTypePage).invalidNec
    }

  def validateSchemeManagersName(answers: UserAnswers): ValidationResult[Option[PersonName]] = {
    val smName = answers.get(SchemeManagersNamePage)
    val smType = answers.get(SchemeManagerTypePage)

    smType match {
      case None =>
        DataMissingError(SchemeManagerTypePage).invalidNec

      case Some(SchemeManagerType.Individual) =>
        smName match {
          case Some(n) => Some(n).validNec
          case None    => DataMissingError(SchemeManagersNamePage).invalidNec
        }

      case Some(SchemeManagerType.Organisation) =>
        smName match {
          case None    => None.validNec
          case Some(_) => GenericError("Individual name must be absent when manager type is org").invalidNec
        }
    }
  }

  def validateSchemeManagersOrgName(answers: UserAnswers): ValidationResult[Option[String]] = {
    val orgName = answers.get(SchemeManagerOrganisationNamePage)
    val smType  = answers.get(SchemeManagerTypePage)

    smType match {
      case None =>
        DataMissingError(SchemeManagerTypePage).invalidNec

      case Some(SchemeManagerType.Organisation) =>
        orgName match {
          case Some(n) => Some(n).validNec
          case None    => DataMissingError(SchemeManagerOrganisationNamePage).invalidNec
        }

      case Some(SchemeManagerType.Individual) =>
        orgName match {
          case None    => None.validNec
          case Some(_) => GenericError("Org name must be absent when manager type is individual").invalidNec
        }
    }
  }

  def validateSchemeOrgContact(answers: UserAnswers): ValidationResult[Option[PersonName]] = {
    val contact = answers.get(SchemeManagerOrgIndividualNamePage)
    val smType  = answers.get(SchemeManagerTypePage)

    smType match {
      case None =>
        DataMissingError(SchemeManagerTypePage).invalidNec

      case Some(SchemeManagerType.Organisation) =>
        contact match {
          case Some(c) => Some(c).validNec
          case None    => DataMissingError(SchemeManagerOrgIndividualNamePage).invalidNec
        }

      case Some(SchemeManagerType.Individual) =>
        contact match {
          case None    => None.validNec
          case Some(_) => GenericError("Org contact must be absent when manager type is individual").invalidNec
        }
    }
  }
}

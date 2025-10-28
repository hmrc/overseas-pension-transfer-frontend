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

import cats.data.Chain
import cats.implicits.{catsSyntaxTuple7Semigroupal, catsSyntaxValidatedIdBinCompat0}
import models.SchemeManagerType.{Individual, Organisation}
import models.address.SchemeManagersAddress
import models.transferJourneys.SchemeManagerDetails
import models.{DataMissingError, GenericError, PersonName, SchemeManagerType, UserAnswers, ValidationResult}
import pages.qropsSchemeManagerDetails._

object SchemeManagerDetailsValidator extends Validator[SchemeManagerDetails] {

  override def fromUserAnswers(user: UserAnswers): ValidationResult[SchemeManagerDetails] = (
    validateSchemeManagerType(user),
    validateSchemeManagersName(user),
    validateSchemeManagersOrgName(user),
    validateSchemeOrgContact(user),
    validateSchemeManagersAddress(user),
    validateSchemeManagersEmail(user),
    validateSchemeManagersPhoneNo(user)
  ).mapN(SchemeManagerDetails)

  def validateSchemeManagerType(answers: UserAnswers): ValidationResult[SchemeManagerType] =
    answers.get(SchemeManagerTypePage) match {
      case Some(smType) => smType.validNec
      case None         => DataMissingError(SchemeManagerTypePage).invalidNec
    }

  def validateSchemeManagersName(answers: UserAnswers): ValidationResult[Option[PersonName]] = {
    val smName = answers.get(SchemeManagersNamePage)
    val smType = answers.get(SchemeManagerTypePage)

    (smType, smName) match {
      case (Some(Individual), Some(name)) => Some(name).validNec
      case (Some(Individual), None)       => DataMissingError(SchemeManagersNamePage).invalidNec
      case (Some(Organisation), None)     => None.validNec
      case (Some(Organisation), Some(_))  => GenericError("Individual name must be absent when manager type is org").invalidNec
      case _                              => DataMissingError(SchemeManagersNamePage).invalidNec
    }
  }

  def validateSchemeManagersOrgName(answers: UserAnswers): ValidationResult[Option[String]] = {
    val orgName = answers.get(SchemeManagerOrganisationNamePage)
    val smType  = answers.get(SchemeManagerTypePage)

    (smType, orgName) match {
      case (Some(Organisation), Some(name)) => Some(name).validNec
      case (Some(Organisation), None)       => DataMissingError(SchemeManagerOrganisationNamePage).invalidNec
      case (Some(Individual), None)         => None.validNec
      case (Some(Individual), Some(_))      => GenericError("Org name must be absent when manager type is individual").invalidNec
      case _                                => DataMissingError(SchemeManagerOrganisationNamePage).invalidNec
    }
  }

  def validateSchemeOrgContact(answers: UserAnswers): ValidationResult[Option[PersonName]] = {
    val contact = answers.get(SchemeManagerOrgIndividualNamePage)
    val smType  = answers.get(SchemeManagerTypePage)

    (smType, contact) match {
      case (Some(Organisation), Some(contactName)) => Some(contactName).validNec
      case (Some(Organisation), None)              => DataMissingError(SchemeManagerOrgIndividualNamePage).invalidNec
      case (Some(Individual), None)                => None.validNec
      case (Some(Individual), Some(_))             => GenericError("Org contact must be absent when manager type is individual").invalidNec
      case _                                       => DataMissingError(SchemeManagerOrgIndividualNamePage).invalidNec
    }
  }

  def validateSchemeManagersAddress(answers: UserAnswers): ValidationResult[SchemeManagersAddress] =
    answers.get(SchemeManagersAddressPage) match {
      case Some(smAdd) => smAdd.validNec
      case None        => DataMissingError(SchemeManagersAddressPage).invalidNec
    }

  def validateSchemeManagersEmail(answers: UserAnswers): ValidationResult[String] =
    answers.get(SchemeManagersEmailPage) match {
      case Some(smE) => smE.validNec
      case None      => DataMissingError(SchemeManagersEmailPage).invalidNec
    }

  def validateSchemeManagersPhoneNo(answers: UserAnswers): ValidationResult[String] =
    answers.get(SchemeManagersContactPage) match {
      case Some(smC) => smC.validNec
      case None      => DataMissingError(SchemeManagersContactPage).invalidNec
    }

  val notStarted: Chain[DataMissingError] = Chain(
    DataMissingError(SchemeManagerTypePage),
    DataMissingError(SchemeManagersNamePage),
    DataMissingError(SchemeManagerOrganisationNamePage),
    DataMissingError(SchemeManagerOrgIndividualNamePage),
    DataMissingError(SchemeManagersAddressPage),
    DataMissingError(SchemeManagersEmailPage),
    DataMissingError(SchemeManagersContactPage)
  )
}

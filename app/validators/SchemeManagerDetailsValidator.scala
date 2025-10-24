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

  private def validateSchemeManagerType(answers: UserAnswers): ValidationResult[SchemeManagerType] =
    answers.get(SchemeManagerTypePage) match {
      case Some(smType) => smType.validNec
      case None         => DataMissingError(SchemeManagerTypePage).invalidNec
    }

  private def validateSchemeManagersName(answers: UserAnswers): ValidationResult[Option[PersonName]] =
    (
      answers.get(SchemeManagersNamePage),
      answers.get(SchemeManagerTypePage),
      answers.get(SchemeManagerOrganisationNamePage),
      answers.get(SchemeManagerOrgIndividualNamePage)
    ) match {
      case (Some(smName), Some(SchemeManagerType.Individual), None, None)  => Some(smName).validNec
      case (None, Some(SchemeManagerType.Organisation), Some(_), Some(_))  => None.validNec
      case (None, Some(SchemeManagerType.Individual), None, None)          => DataMissingError(SchemeManagersNamePage).invalidNec
      case (Some(_), Some(SchemeManagerType.Individual), Some(_), Some(_)) =>
        GenericError("Cannot have valid payload with both (scheme managers name) and (scheme org name, scheme org contact)").invalidNec
      case _                                                               =>
        GenericError("Something went wrong with scheme manager name").invalidNec
    }

  private def validateSchemeManagersOrgName(answers: UserAnswers): ValidationResult[Option[String]] =
    (
      answers.get(SchemeManagersNamePage),
      answers.get(SchemeManagerTypePage),
      answers.get(SchemeManagerOrganisationNamePage),
      answers.get(SchemeManagerOrgIndividualNamePage)
    ) match {
      case (None, Some(SchemeManagerType.Organisation), Some(smOrgName), Some(_))  => Some(smOrgName).validNec
      case (Some(_), Some(SchemeManagerType.Individual), None, None)  => None.validNec
      case (None, Some(SchemeManagerType.Organisation), None, None)          => DataMissingError(SchemeManagerOrganisationNamePage).invalidNec
      case (Some(_), Some(SchemeManagerType.Organisation), Some(_), Some(_)) =>
        GenericError("Cannot have valid payload with both (scheme managers name) and (scheme org name, scheme org contact)").invalidNec
            case _                                                               =>
        GenericError("Something went wrong with scheme manager org name").invalidNec
    }

  private def validateSchemeOrgContact(answers: UserAnswers): ValidationResult[Option[PersonName]] =
    (
      answers.get(SchemeManagersNamePage),
      answers.get(SchemeManagerTypePage),
      answers.get(SchemeManagerOrganisationNamePage),
      answers.get(SchemeManagerOrgIndividualNamePage)
    ) match {
      case (None, Some(SchemeManagerType.Organisation), Some(_), Some(smOrgC))  => Some(smOrgC).validNec
      case (Some(_), Some(SchemeManagerType.Individual), None, None)  => None.validNec
      case (None, Some(SchemeManagerType.Organisation), None, None)          => DataMissingError(SchemeManagerOrgIndividualNamePage).invalidNec
      case (Some(_), Some(SchemeManagerType.Organisation), Some(_), Some(_)) =>
        GenericError("Cannot have valid payload with both (scheme managers name) and (scheme org name, scheme org contact)").invalidNec
      case _                                                               =>
        GenericError("Something went wrong with scheme org contact name").invalidNec
    }

}

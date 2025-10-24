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

import cats.implicits._
import models.address.{MembersCurrentAddress, MembersLastUKAddress}
import models.{DataMissingError, GenericError, PersonName, UserAnswers, ValidationResult}
import models.transferJourneys.MemberDetails
import pages.memberDetails._

import java.time.LocalDate

object MemberDetailsValidator extends Validator[MemberDetails] {

  override def fromUserAnswers(userAnswers: UserAnswers): ValidationResult[MemberDetails] =
    (
      validateMemberName(userAnswers),
      validateMemberNino(userAnswers),
      validateReasonNoNINO(userAnswers),
      validateMemberDateOfBirth(userAnswers),
      validateMemberCurrentAddress(userAnswers),
      validateMemberIsUkResident(userAnswers),
      validateMemberHasEverBeenUkResident(userAnswers),
      validateLastPrincipalUkAddress(userAnswers),
      validateMemberDateLeftUk(userAnswers)
    ).mapN(
      (
          name,
          nino,
          reasonNoNino,
          dateOfBirth,
          currentAddress,
          isUkResident,
          hasEverBeenUkResident,
          lastUkAddress,
          dateLeftUk
      ) =>
        MemberDetails(
          name,
          nino,
          reasonNoNino,
          dateOfBirth,
          currentAddress,
          isUkResident,
          hasEverBeenUkResident,
          lastUkAddress,
          dateLeftUk
        )
    )

  private def validateMemberName(answers: UserAnswers): ValidationResult[PersonName] =
    answers.get(MemberNamePage) match {
      case Some(name) => name.validNec
      case None       => DataMissingError(MemberNamePage).invalidNec
    }

  private def validateMemberNino(answers: UserAnswers): ValidationResult[Option[String]] =
    (answers.get(MemberNinoPage), answers.get(MemberDoesNotHaveNinoPage)) match {
      case (Some(nino), None) => Some(nino).validNec
      case (None, Some(_))    => None.validNec
      case (Some(_), Some(_)) => GenericError("Cannot have valid payload with nino and reasonNoNINO").invalidNec
      case (None, None)       => DataMissingError(MemberNinoPage).invalidNec
    }

  private def validateReasonNoNINO(answers: UserAnswers): ValidationResult[Option[String]] =
    (answers.get(MemberDoesNotHaveNinoPage), answers.get(MemberNinoPage)) match {
      case (Some(nino), None) => Some(nino).validNec
      case (None, Some(_))    => None.validNec
      case (Some(_), Some(_)) => GenericError("Cannot have valid payload with nino and reasonNoNINO").invalidNec
      case (None, None)       => DataMissingError(MemberDoesNotHaveNinoPage).invalidNec
    }

  private def validateMemberDateOfBirth(answers: UserAnswers): ValidationResult[LocalDate] =
    answers.get(MemberDateOfBirthPage) match {
      case Some(date) => date.validNec
      case None       => DataMissingError(MemberDateOfBirthPage).invalidNec
    }

  private def validateMemberCurrentAddress(answers: UserAnswers): ValidationResult[MembersCurrentAddress] =
    answers.get(MembersCurrentAddressPage) match {
      case Some(address) => address.validNec
      case None          => DataMissingError(MembersCurrentAddressPage).invalidNec
    }

  private def validateMemberIsUkResident(answers: UserAnswers): ValidationResult[Boolean] =
    answers.get(MemberIsResidentUKPage) match {
      case Some(isUkResident) => isUkResident.validNec
      case None               => DataMissingError(MemberIsResidentUKPage).invalidNec
    }

  private def validateMemberHasEverBeenUkResident(answers: UserAnswers): ValidationResult[Option[Boolean]] =
    (answers.get(MemberHasEverBeenResidentUKPage), answers.get(MemberIsResidentUKPage)) match {
      case (None, Some(true))                         => None.validNec
      case (None, Some(false))                        => DataMissingError(MemberHasEverBeenResidentUKPage).invalidNec
      case (Some(hasEverBeenUkResident), Some(false)) => Some(hasEverBeenUkResident).validNec
      case (Some(_), Some(true))                      => GenericError("Cannot have valid payload with isUkResident = true and hasEverBeenUkResident").invalidNec
      case (None, None)                               => DataMissingError(MemberHasEverBeenResidentUKPage).invalidNec
    }

  private def validateLastPrincipalUkAddress(answers: UserAnswers): ValidationResult[Option[MembersLastUKAddress]] =
    (answers.get(MemberIsResidentUKPage), answers.get(MemberHasEverBeenResidentUKPage)) match {
      case (Some(true), None)         => None.validNec
      case (Some(true), Some(_))      => GenericError("Cannot have valid payload with isUkResident = true and lastUkPrincipalAddress").invalidNec
      case (Some(false), Some(false)) => None.validNec
      case (Some(false), Some(true))  =>
        answers.get(MembersLastUKAddressPage) match {
          case Some(date) => Some(date).validNec
          case None       => DataMissingError(MembersLastUKAddressPage).invalidNec
        }
      case _                          => DataMissingError(MembersLastUKAddressPage).invalidNec
    }

  private def validateMemberDateLeftUk(answers: UserAnswers): ValidationResult[Option[LocalDate]] =
    (answers.get(MemberIsResidentUKPage), answers.get(MemberHasEverBeenResidentUKPage)) match {
      case (Some(true), None)         => None.validNec
      case (Some(true), Some(_))      => GenericError("Cannot have valid payload with isUkResident = true and memberDateLeftUk").invalidNec
      case (Some(false), Some(false)) => None.validNec
      case (Some(false), Some(true))  =>
        answers.get(MemberDateOfLeavingUKPage) match {
          case Some(date) => Some(date).validNec
          case None       => DataMissingError(MemberDateOfLeavingUKPage).invalidNec
        }
      case _                          => DataMissingError(MemberDateOfLeavingUKPage).invalidNec
    }
}

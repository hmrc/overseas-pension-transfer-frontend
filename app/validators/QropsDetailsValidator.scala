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

import cats.implicits.{catsSyntaxTuple5Semigroupal, catsSyntaxValidatedIdBinCompat0}
import models.address.{Country, QROPSAddress}
import models.transferJourneys.QropsDetails
import models.{DataMissingError, GenericError, UserAnswers, ValidationResult}
import pages.qropsDetails._

object QropsDetailsValidator extends Validator[QropsDetails] {

  override def fromUserAnswers(user: UserAnswers): ValidationResult[QropsDetails] =
    (
      validateQropsName(user),
      validateQropsReference(user),
      validateQropsAddress(user),
      validateQropsCountry(user),
      validateQropsOtherCountry(user)
    ).mapN(
      QropsDetails
    )

  private def validateQropsName(answers: UserAnswers): ValidationResult[String] =
    answers.get(QROPSNamePage) match {
      case Some(name) => name.validNec
      case None       => DataMissingError(QROPSNamePage).invalidNec
    }

  private def validateQropsReference(answers: UserAnswers): ValidationResult[String] =
    answers.get(QROPSReferencePage) match {
      case Some(name) => name.validNec
      case None       => DataMissingError(QROPSReferencePage).invalidNec
    }

  private def validateQropsAddress(answers: UserAnswers): ValidationResult[QROPSAddress] =
    answers.get(QROPSAddressPage) match {
      case Some(name) => name.validNec
      case None       => DataMissingError(QROPSAddressPage).invalidNec
    }

  private def validateQropsCountry(answers: UserAnswers): ValidationResult[Option[Country]] =
    (answers.get(QROPSCountryPage), answers.get(QROPSOtherCountryPage)) match {
      case (Some(country), None) => Some(country).validNec
      case (None, Some(_))       => None.validNec
      case (Some(_), Some(_))    => GenericError("Cannot have valid payload with selected country and other country").invalidNec
      case (None, None)          => DataMissingError(QROPSCountryPage).invalidNec
    }

  private def validateQropsOtherCountry(answers: UserAnswers): ValidationResult[Option[String]] =
    (answers.get(QROPSOtherCountryPage), answers.get(QROPSCountryPage)) match {
      case (Some(oCountry), None) => Some(oCountry).validNec
      case (None, Some(_))        => None.validNec
      case (Some(_), Some(_))     => GenericError("Cannot have valid payload with other country and selected country").invalidNec
      case (None, None)           => DataMissingError(QROPSOtherCountryPage).invalidNec
    }

}

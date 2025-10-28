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

package validators.assetsValidators

import cats.data.Validated.Valid
import cats.implicits.{catsSyntaxTuple3Semigroupal, catsSyntaxValidatedIdBinCompat0}
import models.address.PropertyAddress
import models.assets.{PropertyEntry, TypeOfAsset}
import models.{DataMissingError, GenericError, UserAnswers, ValidationResult}
import pages.transferDetails.TypeOfAssetPage
import pages.transferDetails.assetsMiniJourneys.property.{PropertyAddressPage, PropertyDescriptionPage, PropertyValuePage}
import queries.assets.PropertyQuery

object PropertyValidator {

  private def validatePropertyAddress(answers: UserAnswers, index: Int = 0): ValidationResult[PropertyAddress] =
    answers.get(PropertyAddressPage(index)) match {
      case Some(propertyAddress) => propertyAddress.validNec
      case None                  => DataMissingError(PropertyAddressPage(index)).invalidNec
    }

  private def validatePropertyValue(answers: UserAnswers, index: Int = 0): ValidationResult[BigDecimal] =
    answers.get(PropertyValuePage(index)) match {
      case Some(propertyValue) => propertyValue.validNec
      case None                => DataMissingError(PropertyValuePage(index)).invalidNec
    }

  private def validatePropertyDescription(answers: UserAnswers, index: Int = 0): ValidationResult[String] =
    answers.get(PropertyDescriptionPage(index)) match {
      case Some(propertyDescription) => propertyDescription.validNec
      case None                      => DataMissingError(PropertyDescriptionPage(index)).invalidNec
    }

  def validatePropertyDetails(answers: UserAnswers): ValidationResult[Option[List[PropertyEntry]]] = {
    answers.get(TypeOfAssetPage) match {
      case Some(assets) if assets.contains(TypeOfAsset.Property) =>
        answers.get(PropertyQuery) match {
          case Some(shares) if shares.length > 5 => GenericError("Property cannot hold more than 5 in list").invalidNec
          case Some(shares)                      =>
            val validatedShares = shares.zipWithIndex map {
              case (PropertyEntry(_, _, _), index) =>
                (
                  validatePropertyAddress(answers, index),
                  validatePropertyValue(answers, index),
                  validatePropertyDescription(answers, index)
                ).mapN(PropertyEntry.apply)
              case _                               => GenericError("not recognised value as property entry").invalidNec
            }

            validatedShares.filter(validatedShare => validatedShare == Valid(PropertyEntry(_, _, _))) match {
              case Nil     => Some(shares).validNec
              case List(_) => GenericError("errors found with validating property entry").invalidNec
            }

          case None => DataMissingError(PropertyQuery).invalidNec
        }
      case Some(_)                                               => None.validNec
      case None                                                  => DataMissingError(PropertyQuery).invalidNec
    }
  }
}

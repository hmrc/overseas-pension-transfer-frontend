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
import cats.implicits.{catsSyntaxTuple2Semigroupal, catsSyntaxValidatedIdBinCompat0}
import models.assets.{OtherAssetsEntry, TypeOfAsset}
import models.{DataMissingError, GenericError, UserAnswers, ValidationResult}
import pages.transferDetails.TypeOfAssetPage
import pages.transferDetails.assetsMiniJourneys.otherAssets.{OtherAssetsDescriptionPage, OtherAssetsValuePage}
import queries.assets.OtherAssetsQuery

object OtherAssetsValidator {

  private def validateOtherAssetsDescription(answers: UserAnswers, index: Int = 0): ValidationResult[String] =
    answers.get(OtherAssetsDescriptionPage(index)) match {
      case Some(otherAssetsDescription) => otherAssetsDescription.validNec
      case None                         => DataMissingError(OtherAssetsDescriptionPage(index)).invalidNec
    }

  private def validateOtherAssetsValue(answers: UserAnswers, index: Int = 0): ValidationResult[BigDecimal] =
    answers.get(OtherAssetsValuePage(index)) match {
      case Some(otherAssetsValue) => otherAssetsValue.validNec
      case None                   => DataMissingError(OtherAssetsValuePage(index)).invalidNec
    }

  def validateOtherAssetsDetails(answers: UserAnswers): ValidationResult[Option[List[OtherAssetsEntry]]] = {
    answers.get(TypeOfAssetPage) match {
      case Some(assets) if assets.contains(TypeOfAsset.Other) =>
        answers.get(OtherAssetsQuery) match {
          case Some(shares) if shares.length > 5 => GenericError("Other assets cannot hold more than 5 in list").invalidNec
          case Some(shares)                      =>
            val validatedShares = shares.zipWithIndex map {
              case (OtherAssetsEntry(_, _), index) =>
                (
                  validateOtherAssetsDescription(answers, index),
                  validateOtherAssetsValue(answers, index)
                ).mapN(OtherAssetsEntry.apply)
              case _                               => GenericError("not recognised value as other assets entry").invalidNec
            }

            validatedShares.filter(validatedShare => validatedShare == Valid(OtherAssetsEntry(_, _))) match {
              case Nil     => Some(shares).validNec
              case List(_) => GenericError("errors found with validating other assets entry").invalidNec
            }

          case None => DataMissingError(OtherAssetsQuery).invalidNec
        }
      case Some(_)                                            => None.validNec
      case None                                               => DataMissingError(OtherAssetsQuery).invalidNec
    }
  }

}

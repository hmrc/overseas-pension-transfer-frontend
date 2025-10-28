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
import cats.implicits.{catsSyntaxTuple4Semigroupal, catsSyntaxValidatedIdBinCompat0}
import models.{DataMissingError, GenericError, UserAnswers, ValidationResult}
import models.assets.{TypeOfAsset, UnquotedSharesEntry}
import pages.transferDetails.TypeOfAssetPage
import pages.transferDetails.assetsMiniJourneys.unquotedShares.{
  UnquotedSharesClassPage,
  UnquotedSharesCompanyNamePage,
  UnquotedSharesNumberPage,
  UnquotedSharesValuePage
}
import queries.assets.UnquotedSharesQuery

object UnquotedSharesValidator {

  private def validateUnquotedSharesCompanyName(answers: UserAnswers, index: Int = 0): ValidationResult[String] =
    answers.get(UnquotedSharesCompanyNamePage(index)) match {
      case Some(unquotedCompanyName) => unquotedCompanyName.validNec
      case None                      => DataMissingError(UnquotedSharesCompanyNamePage(index)).invalidNec
    }

  private def validateUnquotedSharesValue(answers: UserAnswers, index: Int = 0): ValidationResult[BigDecimal] =
    answers.get(UnquotedSharesValuePage(index)) match {
      case Some(unquotedShareValue) => unquotedShareValue.validNec
      case None                     => DataMissingError(UnquotedSharesValuePage(index)).invalidNec
    }

  private def validateUnquotedSharesNumber(answers: UserAnswers, index: Int = 0): ValidationResult[Int] =
    answers.get(UnquotedSharesNumberPage(index)) match {
      case Some(unquotedNumberOfShares) => unquotedNumberOfShares.validNec
      case None                         => DataMissingError(UnquotedSharesNumberPage(index)).invalidNec
    }

  private def validateUnquotedSharesClass(answers: UserAnswers, index: Int = 0): ValidationResult[String] =
    answers.get(UnquotedSharesClassPage(index)) match {
      case Some(unquotedShareClass) => unquotedShareClass.validNec
      case None                     => DataMissingError(UnquotedSharesClassPage(index)).invalidNec
    }

  def validateUnquotedShares(answers: UserAnswers): ValidationResult[Option[List[UnquotedSharesEntry]]] = {
    answers.get(TypeOfAssetPage) match {
      case Some(assets) if assets.contains(TypeOfAsset.UnquotedShares) =>
        answers.get(UnquotedSharesQuery) match {
          case Some(shares) if shares.length > 5 => GenericError("Unquoted shares cannot hold more than 5 in list").invalidNec
          case Some(shares)                      =>
            val validatedShares = shares.zipWithIndex map {
              case (UnquotedSharesEntry(_, _, _, _), index) =>
                (
                  validateUnquotedSharesCompanyName(answers, index),
                  validateUnquotedSharesValue(answers, index),
                  validateUnquotedSharesNumber(answers, index),
                  validateUnquotedSharesClass(answers, index)
                ).mapN(UnquotedSharesEntry.apply)
              case _                                        => GenericError("not recognised value as unquoted shares").invalidNec
            }

            validatedShares.filter(validatedShare => validatedShare == Valid(UnquotedSharesEntry(_, _, _, _))) match {
              case Nil     => Some(shares).validNec
              case List(_) => GenericError("errors found with validating UnquotedShares").invalidNec
            }

          case None => DataMissingError(UnquotedSharesQuery).invalidNec
        }
      case Some(_)                                                     => None.validNec
      case None                                                        => DataMissingError(UnquotedSharesQuery).invalidNec
    }
  }
}

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
import models.assets.{QuotedSharesEntry, TypeOfAsset}
import models.{DataMissingError, GenericError, UserAnswers, ValidationResult}
import pages.transferDetails.TypeOfAssetPage
import pages.transferDetails.assetsMiniJourneys.quotedShares.{QuotedSharesClassPage, QuotedSharesCompanyNamePage, QuotedSharesNumberPage, QuotedSharesValuePage}
import queries.assets.QuotedSharesQuery

object QuotedSharesValidator {

  private def validateQuotedSharesCompanyName(answers: UserAnswers, index: Int = 0): ValidationResult[String] =
    answers.get(QuotedSharesCompanyNamePage(index)) match {
      case Some(quotedCompanyName) => quotedCompanyName.validNec
      case None                    => DataMissingError(QuotedSharesCompanyNamePage(index)).invalidNec
    }

  private def validateQuotedSharesValue(answers: UserAnswers, index: Int = 0): ValidationResult[BigDecimal] =
    answers.get(QuotedSharesValuePage(index)) match {
      case Some(quotedShareValue) => quotedShareValue.validNec
      case None                   => DataMissingError(QuotedSharesValuePage(index)).invalidNec
    }

  private def validateQuotedSharesNumber(answers: UserAnswers, index: Int = 0): ValidationResult[Int] =
    answers.get(QuotedSharesNumberPage(index)) match {
      case Some(quotedNumberOfShares) => quotedNumberOfShares.validNec
      case None                       => DataMissingError(QuotedSharesNumberPage(index)).invalidNec
    }

  private def validateQuotedSharesClass(answers: UserAnswers, index: Int = 0): ValidationResult[String] =
    answers.get(QuotedSharesClassPage(index)) match {
      case Some(quotedShareClass) => quotedShareClass.validNec
      case None                   => DataMissingError(QuotedSharesClassPage(index)).invalidNec
    }

  def validateQuotedShares(answers: UserAnswers): ValidationResult[Option[List[QuotedSharesEntry]]] = {
    answers.get(TypeOfAssetPage) match {
      case Some(assets) if assets.contains(TypeOfAsset.QuotedShares) =>
        answers.get(QuotedSharesQuery) match {
          case Some(shares) if shares.length > 5 => GenericError("Unquoted shares cannot hold more than 5 in list").invalidNec
          case Some(shares)                      =>
            val validatedShares = shares.zipWithIndex map {
              case (QuotedSharesEntry(_, _, _, _), index) =>
                (
                  validateQuotedSharesCompanyName(answers, index),
                  validateQuotedSharesValue(answers, index),
                  validateQuotedSharesNumber(answers, index),
                  validateQuotedSharesClass(answers, index)
                ).mapN(QuotedSharesEntry.apply)
              case _                                      => GenericError("not recognised value as unquoted shares").invalidNec
            }

            validatedShares.filter(validatedShare => validatedShare == Valid(QuotedSharesEntry(_, _, _, _))) match {
              case Nil     => Some(shares).validNec
              case List(_) => GenericError("errors found with validating UnquotedShares").invalidNec
            }

          case None => DataMissingError(QuotedSharesQuery).invalidNec
        }
      case Some(_)                                                   => None.validNec
      case None                                                      => DataMissingError(QuotedSharesQuery).invalidNec
    }
  }
}

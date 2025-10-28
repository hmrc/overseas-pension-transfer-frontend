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
import cats.implicits._
import models.assets.TypeOfAsset
import models.assets.TypeOfAsset._
import models.transferJourneys._
import models.{ApplicableTaxExclusions, DataMissingError, GenericError, UserAnswers, ValidationResult, WhyTransferIsNotTaxable, WhyTransferIsTaxable}
import pages.transferDetails._
import pages.transferDetails.assetsMiniJourneys.cash.CashAmountInTransferPage
import pages.transferDetails.assetsMiniJourneys.otherAssets.MoreOtherAssetsDeclarationPage
import pages.transferDetails.assetsMiniJourneys.property.MorePropertyDeclarationPage
import pages.transferDetails.assetsMiniJourneys.quotedShares.MoreQuotedSharesDeclarationPage
import pages.transferDetails.assetsMiniJourneys.unquotedShares.MoreUnquotedSharesDeclarationPage
import queries.assets._
import validators.assetsValidators.{OtherAssetsValidator, PropertyValidator, QuotedSharesValidator, UnquotedSharesValidator}

import java.time.LocalDate

object TransferDetailsValidator extends Validator[TransferDetails] {

  override def fromUserAnswers(userAnswers: UserAnswers): ValidationResult[TransferDetails] = {
    (
      validateAllowanceBeforeTransfer(userAnswers),
      validateTransferAmount(userAnswers),
      validateIsTransferTaxable(userAnswers),
      validateWhyIsTransferTaxable(userAnswers),
      validateWhyIsTransferNotTaxable(userAnswers),
      validateApplicableTaxExclusions(userAnswers),
      validateAmountOfTaxDeducted(userAnswers),
      validateNetTransferAmount(userAnswers),
      validateDateOfTransfer(userAnswers),
      validateIsTransferCashOnly(userAnswers),
      validateTypeOfAsset(userAnswers),
      validateCashAmountInTransfer(userAnswers),
      UnquotedSharesValidator.validateUnquotedShares(userAnswers),
      validateMoreThan5Unquoted(userAnswers),
      QuotedSharesValidator.validateQuotedShares(userAnswers),
      validateMoreThan5Quoted(userAnswers),
      PropertyValidator.validatePropertyDetails(userAnswers),
      validateMoreThan5Property(userAnswers),
      OtherAssetsValidator.validateOtherAssetsDetails(userAnswers),
      validateMoreThan5Other(userAnswers)
    ).mapN(TransferDetails.apply)
  }

  private def validateAllowanceBeforeTransfer(answers: UserAnswers): ValidationResult[BigDecimal] =
    answers.get(OverseasTransferAllowancePage) match {
      case Some(allowanceBeforeTransfer) => allowanceBeforeTransfer.validNec
      case None                          => DataMissingError(OverseasTransferAllowancePage).invalidNec
    }

  private def validateTransferAmount(answers: UserAnswers): ValidationResult[BigDecimal] =
    answers.get(AmountOfTransferPage) match {
      case Some(transferAmount) => transferAmount.validNec
      case None                 => DataMissingError(AmountOfTransferPage).invalidNec
    }

  private def validateIsTransferTaxable(answers: UserAnswers): ValidationResult[Boolean] =
    answers.get(IsTransferTaxablePage) match {
      case Some(isTaxable) => isTaxable.validNec
      case None            => DataMissingError(IsTransferTaxablePage).invalidNec
    }

  private def validateWhyIsTransferTaxable(answers: UserAnswers): ValidationResult[WhyTransferIsTaxable] =
    answers.get(IsTransferTaxablePage) match {
      case Some(true)  =>
        answers.get(WhyTransferIsTaxablePage) match {
          case Some(whyTaxable) => whyTaxable.validNec
          case None             => DataMissingError(WhyTransferIsTaxablePage).invalidNec
        }
      case Some(false) =>
        DataMissingError(WhyTransferIsTaxablePage).invalidNec
      case None        =>
        DataMissingError(IsTransferTaxablePage).invalidNec
    }

  private def validateWhyIsTransferNotTaxable(answers: UserAnswers): ValidationResult[Set[WhyTransferIsNotTaxable]] =
    answers.get(IsTransferTaxablePage) match {
      case Some(false) =>
        answers.get(WhyTransferIsNotTaxablePage) match {
          case Some(whyNotTaxable) if whyNotTaxable.nonEmpty => whyNotTaxable.validNec
          case _                                             => DataMissingError(WhyTransferIsNotTaxablePage).invalidNec
        }
      case Some(true)  =>
        Set.empty[WhyTransferIsNotTaxable].validNec
      case None        =>
        DataMissingError(IsTransferTaxablePage).invalidNec
    }

  private def validateApplicableTaxExclusions(answers: UserAnswers): ValidationResult[Set[ApplicableTaxExclusions]] =
    (answers.get(IsTransferTaxablePage), answers.get(WhyTransferIsTaxablePage)) match {
      case (Some(true), Some(WhyTransferIsTaxable.TransferExceedsOTCAllowance)) =>
        answers.get(ApplicableTaxExclusionsPage) match {
          case Some(exclusions) if exclusions.nonEmpty => exclusions.validNec
          case _                                       => DataMissingError(ApplicableTaxExclusionsPage).invalidNec
        }
      case _                                                                    => Set.empty[ApplicableTaxExclusions].validNec
    }

  private def validateAmountOfTaxDeducted(answers: UserAnswers): ValidationResult[BigDecimal] =
    answers.get(AmountOfTaxDeductedPage) match {
      case Some(amountOfTaxDeducted) => amountOfTaxDeducted.validNec
      case None                      => DataMissingError(AmountOfTaxDeductedPage).invalidNec
    }

  private def validateNetTransferAmount(answers: UserAnswers): ValidationResult[BigDecimal] =
    answers.get(NetTransferAmountPage) match {
      case Some(netTransferAmount) => netTransferAmount.validNec
      case None                    => DataMissingError(NetTransferAmountPage).invalidNec
    }

  private def validateDateOfTransfer(answers: UserAnswers): ValidationResult[LocalDate] =
    answers.get(DateOfTransferPage) match {
      case Some(dateOfTransfer) => dateOfTransfer.validNec
      case None                 => DataMissingError(DateOfTransferPage).invalidNec
    }

  private def validateIsTransferCashOnly(answers: UserAnswers): ValidationResult[Boolean] =
    answers.get(IsTransferCashOnlyPage) match {
      case Some(isTransferCashOnly) => isTransferCashOnly.validNec
      case None                     => DataMissingError(IsTransferCashOnlyPage).invalidNec
    }

  private def validateTypeOfAsset(answers: UserAnswers): ValidationResult[Seq[TypeOfAsset]] =
    answers.get(TypeOfAssetPage) match {
      case Some(asset) => asset.validNec
      case None        => DataMissingError(TypeOfAssetPage).invalidNec
    }

  private def validateCashAmountInTransfer(
      answers: UserAnswers
    ): ValidationResult[Option[BigDecimal]] = {
    answers.get(TypeOfAssetPage) match {
      case Some(assets) if assets.contains(TypeOfAsset.Cash) =>
        answers.get(CashAmountInTransferPage) match {
          case Some(cashAmountInTransfer) if cashAmountInTransfer > 0 => Some(cashAmountInTransfer).validNec
          case _                                                      => DataMissingError(CashAmountInTransferPage).invalidNec
        }
      case Some(_)                                           => None.validNec
      case None                                              => DataMissingError(CashAmountInTransferPage).invalidNec
    }
  }

  private def validateMoreThan5Unquoted(answers: UserAnswers): ValidationResult[Option[Boolean]] =
    answers.get(TypeOfAssetPage) match {
      case Some(assets) if assets.contains(UnquotedShares) =>
        (answers.get(UnquotedSharesQuery), answers.get(MoreUnquotedSharesDeclarationPage)) match {
          case (Some(shares), Some(value)) if shares.length == 5 => Some(value).validNec
          case (Some(shares), None) if shares.length == 5        => DataMissingError(MoreUnquotedSharesDeclarationPage).invalidNec
          case (None, Some(_))                                   => GenericError("moreUnquoted cannot be populated when no unquoted shares are present").invalidNec
          case _                                                 => None.validNec
        }
      case Some(_)                                         => None.validNec
      case None                                            => DataMissingError(MoreUnquotedSharesDeclarationPage).invalidNec
    }

  private def validateMoreThan5Quoted(answers: UserAnswers): ValidationResult[Option[Boolean]] =
    answers.get(TypeOfAssetPage) match {
      case Some(assets) if assets.contains(QuotedShares) =>
        (answers.get(QuotedSharesQuery), answers.get(MoreQuotedSharesDeclarationPage)) match {
          case (Some(shares), Some(value)) if shares.length == 5 => Some(value).validNec
          case (Some(shares), None) if shares.length == 5        => DataMissingError(MoreQuotedSharesDeclarationPage).invalidNec
          case (None, Some(_))                                   => GenericError("moreQuoted cannot be populated when no quoted shares are present").invalidNec
          case _                                                 => None.validNec
        }
      case Some(_)                                       => None.validNec
      case None                                          => DataMissingError(MoreQuotedSharesDeclarationPage).invalidNec
    }

  private def validateMoreThan5Property(answers: UserAnswers): ValidationResult[Option[Boolean]] =
    answers.get(TypeOfAssetPage) match {
      case Some(assets) if assets.contains(Property) =>
        (answers.get(PropertyQuery), answers.get(MorePropertyDeclarationPage)) match {
          case (Some(shares), Some(value)) if shares.length == 5 => Some(value).validNec
          case (Some(shares), None) if shares.length == 5        => DataMissingError(MorePropertyDeclarationPage).invalidNec
          case (None, Some(_))                                   => GenericError("moreProp cannot be populated when no properties are present").invalidNec
          case _                                                 => None.validNec
        }
      case Some(_)                                   => None.validNec
      case None                                      => DataMissingError(MorePropertyDeclarationPage).invalidNec
    }

  private def validateMoreThan5Other(answers: UserAnswers): ValidationResult[Option[Boolean]] =
    answers.get(TypeOfAssetPage) match {
      case Some(assets) if assets.contains(Other) =>
        (answers.get(OtherAssetsQuery), answers.get(MoreOtherAssetsDeclarationPage)) match {
          case (Some(shares), Some(value)) if shares.length == 5 => Some(value).validNec
          case (Some(shares), None) if shares.length == 5        => DataMissingError(MoreOtherAssetsDeclarationPage).invalidNec
          case (None, Some(_))                                   => GenericError("moreQuoted cannot be populated when no unquoted shares are present").invalidNec
          case _                                                 => None.validNec
        }
      case Some(_)                                => None.validNec
      case None                                   => DataMissingError(MoreOtherAssetsDeclarationPage).invalidNec
    }

  val notStarted: Chain[DataMissingError] = Chain(
    DataMissingError(OverseasTransferAllowancePage),
    DataMissingError(AmountOfTransferPage),
    DataMissingError(IsTransferTaxablePage),
    DataMissingError(IsTransferTaxablePage),
    DataMissingError(IsTransferTaxablePage),
    DataMissingError(AmountOfTaxDeductedPage),
    DataMissingError(NetTransferAmountPage),
    DataMissingError(DateOfTransferPage),
    DataMissingError(IsTransferCashOnlyPage),
    DataMissingError(TypeOfAssetPage),
    DataMissingError(CashAmountInTransferPage),
    DataMissingError(UnquotedSharesQuery),
    DataMissingError(MoreUnquotedSharesDeclarationPage),
    DataMissingError(QuotedSharesQuery),
    DataMissingError(MoreQuotedSharesDeclarationPage),
    DataMissingError(PropertyQuery),
    DataMissingError(MorePropertyDeclarationPage),
    DataMissingError(OtherAssetsQuery),
    DataMissingError(MoreOtherAssetsDeclarationPage)
  )
}

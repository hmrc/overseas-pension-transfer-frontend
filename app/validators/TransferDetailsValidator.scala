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
import models.address.PropertyAddress
import models.assets.TypeOfAsset
import models.transferJourneys.{OtherAssetsDetails, PropertyDetails, QuotedSharesDetails, TransferDetails, UnquotedSharesDetails}
import models.{ApplicableTaxExclusions, DataMissingError, UserAnswers, ValidationResult, WhyTransferIsNotTaxable, WhyTransferIsTaxable}
import pages.transferDetails._
import pages.transferDetails.assetsMiniJourneys.cash.CashAmountInTransferPage
import pages.transferDetails.assetsMiniJourneys.otherAssets.{OtherAssetsDescriptionPage, OtherAssetsValuePage}
import pages.transferDetails.assetsMiniJourneys.property.{PropertyAddressPage, PropertyDescriptionPage, PropertyValuePage}
import pages.transferDetails.assetsMiniJourneys.quotedShares.{QuotedSharesClassPage, QuotedSharesCompanyNamePage, QuotedSharesNumberPage, QuotedSharesValuePage}
import pages.transferDetails.assetsMiniJourneys.unquotedShares.{
  UnquotedSharesClassPage,
  UnquotedSharesCompanyNamePage,
  UnquotedSharesNumberPage,
  UnquotedSharesValuePage
}

import java.time.LocalDate

object TransferDetailsValidator extends Validator[TransferDetails] {

  override def fromUserAnswers(userAnswers: UserAnswers): ValidationResult[TransferDetails] = {
    val selectedAssets = userAnswers.get(TypeOfAssetPage).getOrElse(Seq.empty)

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
      validateCashAmountInTransfer(userAnswers), {
        if (selectedAssets.contains(TypeOfAsset.Other)) {
          validateOtherAssetsDetails(userAnswers)
        } else {
          List.empty[OtherAssetsDetails].validNec
        }
      }, {
        if (selectedAssets.contains(TypeOfAsset.Property)) {
          validatePropertyDetails(userAnswers)
        } else {
          None.validNec
        }
      }, {
        if (selectedAssets.contains(TypeOfAsset.UnquotedShares)) {
          validateUnquotedShares(userAnswers)
        } else {
          None.validNec
        }
      }, {
        if (selectedAssets.contains(TypeOfAsset.QuotedShares)) {
          validateQuotedShares(userAnswers)
        } else {
          None.validNec
        }
      }
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
      case _                                                 => None.validNec
    }
  }

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

  private def validateUnquotedShares(answers: UserAnswers, index: Int = 0): ValidationResult[Option[UnquotedSharesDetails]] = {
    answers.get(TypeOfAssetPage) match {
      case Some(assets) if assets.contains(TypeOfAsset.UnquotedShares) =>
        (
          validateUnquotedSharesCompanyName(answers, index),
          validateUnquotedSharesValue(answers, index),
          validateUnquotedSharesNumber(answers, index),
          validateUnquotedSharesClass(answers, index)
        ).mapN { case (companyName, value, numberOfShares, shareClass) =>
          Some(UnquotedSharesDetails(
            unquotedCompanyName    = companyName,
            unquotedShareValue     = value,
            unquotedNumberOfShares = numberOfShares,
            unquotedShareClass     = shareClass
          ))
        }
      case _                                                           => None.validNec
    }
  }

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

  private def validateQuotedShares(answers: UserAnswers, index: Int = 0): ValidationResult[Option[QuotedSharesDetails]] = {
    answers.get(TypeOfAssetPage) match {
      case Some(assets) if assets.contains(TypeOfAsset.QuotedShares) =>
        (
          validateQuotedSharesCompanyName(answers, index),
          validateQuotedSharesValue(answers, index),
          validateQuotedSharesNumber(answers, index),
          validateQuotedSharesClass(answers, index)
        ).mapN { case (companyName, value, numberOfShares, shareClass) =>
          Some(QuotedSharesDetails(
            quotedCompanyName    = companyName,
            quotedShareValue     = value,
            quotedNumberOfShares = numberOfShares,
            quotedShareClass     = shareClass
          ))
        }
      case _                                                         => None.validNec
    }
  }

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

  private def validatePropertyDetails(answers: UserAnswers, index: Int = 0): ValidationResult[Option[PropertyDetails]] = {
    answers.get(TypeOfAssetPage) match {
      case Some(assets) if assets.contains(TypeOfAsset.Property) =>
        (
          validatePropertyAddress(answers, index),
          validatePropertyValue(answers, index),
          validatePropertyDescription(answers, index)
        ).mapN { case (address, value, description) =>
          Some(PropertyDetails(
            propertyAddress     = address,
            propertyValue       = value,
            propertyDescription = description
          ))
        }
      case _                                                     => None.validNec
    }
  }

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

  private def validateOtherAssetsDetails(answers: UserAnswers): ValidationResult[List[OtherAssetsDetails]] = {
    answers.get(TypeOfAssetPage) match {
      case Some(assets) if assets.contains(TypeOfAsset.Other) =>
        val assetIndices = (0 until 4).filter { index =>
          answers.get(OtherAssetsDescriptionPage(index)).isDefined ||
          answers.get(OtherAssetsValuePage(index)).isDefined
        }.toSet

        val validatedAssets = assetIndices.toList.traverse { index =>
          (
            validateOtherAssetsDescription(answers, index),
            validateOtherAssetsValue(answers, index)
          ).mapN { case (description, value) =>
            OtherAssetsDetails(
              otherAssetsDescription = description,
              otherAssetsValue       = value
            )
          }
        }

        validatedAssets.andThen { assets =>
          if (assets.nonEmpty) {
            assets.validNec
          } else {
            DataMissingError(OtherAssetsDescriptionPage(0)).invalidNec
          }
        }
      case _                                                  =>
        DataMissingError(TypeOfAssetPage).invalidNec
    }
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
    DataMissingError(TypeOfAssetPage)
  )
}

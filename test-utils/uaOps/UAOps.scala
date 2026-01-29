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

package uaOps

import pages.qropsDetails._
import models.address.{Countries, Country, PropertyAddress, QROPSAddress, SchemeManagersAddress}
import models.{PersonName, SchemeManagerType, UserAnswers}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import pages.qropsSchemeManagerDetails._
import pages.transferDetails.assetsMiniJourneys.otherAssets.{OtherAssetsDescriptionPage, OtherAssetsValuePage}
import pages.transferDetails.assetsMiniJourneys.property.{PropertyAddressPage, PropertyDescriptionPage, PropertyValuePage}
import pages.transferDetails.assetsMiniJourneys.quotedShares.{QuotedSharesClassPage, QuotedSharesCompanyNamePage, QuotedSharesNumberPage, QuotedSharesValuePage}
import pages.transferDetails.assetsMiniJourneys.unquotedShares.{
  UnquotedSharesClassPage,
  UnquotedSharesCompanyNamePage,
  UnquotedSharesNumberPage,
  UnquotedSharesValuePage
}

object UAOps {

  implicit final class QropsAnswersOps(private val ua: UserAnswers) extends AnyVal {

    def withName(value: String): UserAnswers =
      ua.set(QROPSNamePage, value).success.value

    def withRef(value: String): UserAnswers =
      ua.set(QROPSReferencePage, value).success.value

    def withAddress(value: QROPSAddress): UserAnswers =
      ua.set(QROPSAddressPage, value).success.value

    def withCountry(value: Country): UserAnswers =
      ua.set(QROPSCountryPage, value).success.value

    def withOtherCountry(value: String): UserAnswers =
      ua.set(QROPSOtherCountryPage, value).success.value
  }

  implicit final class SchemeManagerAnswersOps(private val ua: UserAnswers) extends AnyVal {

    def withSchemeManagerType(value: SchemeManagerType): UserAnswers =
      ua.set(SchemeManagerTypePage, value).success.value

    def withSchemeManagersName(value: PersonName): UserAnswers =
      ua.set(SchemeManagersNamePage, value).success.value

    def withSchemeManagerOrganisationName(value: String): UserAnswers =
      ua.set(SchemeManagerOrganisationNamePage, value).success.value

    def withSchemeManagerOrgContact(value: PersonName): UserAnswers =
      ua.set(SchemeManagerOrgIndividualNamePage, value).success.value

    def withAddress(value: SchemeManagersAddress): UserAnswers =
      ua.set(SchemeManagersAddressPage, value).success.value

    def withSchemeManagersEmail(value: String): UserAnswers =
      ua.set(SchemeManagersEmailPage, value).success.value

    def withSchemeManagersPhoneNo(value: String): UserAnswers =
      ua.set(SchemeManagersContactPage, value).success.value
  }

  implicit final class Assets(private val ua: UserAnswers) extends AnyVal {

    def withOtherAsset(idx: Int): UserAnswers =
      ua.set(
        OtherAssetsDescriptionPage(idx),
        s"OtherAsset-${idx + 1}"
      ).success.value
        .set(OtherAssetsValuePage(idx), BigDecimal(200 + idx * 50))
        .success.value

    def withPropertyAsset(idx: Int): UserAnswers =
      ua.set(
        PropertyAddressPage(idx),
        PropertyAddress(
          addressLine1 = s"${idx + 1} Property${idx + 1}",
          addressLine2 = s"Test address line ${idx + 1}",
          None,
          None,
          None,
          Countries.UK,
          None
        )
      ).success.value
        .set(PropertyValuePage(idx), BigDecimal(10000 + idx * 1000)) // incremental property values
        .success.value
        .set(PropertyDescriptionPage(idx), s"Description-${idx + 1}")
        .success.value

    def withQuotedSharesAsset(idx: Int): UserAnswers =
      ua.set(
        QuotedSharesCompanyNamePage(idx),
        s"QuotedCompany-${idx + 1}"
      ).success.value
        .set(QuotedSharesValuePage(idx), BigDecimal(500 + idx * 125))
        .success.value
        .set(QuotedSharesNumberPage(idx), 600 + idx * 150)
        .success.value
        .set(QuotedSharesClassPage(idx), "B")
        .success.value

    def withUnquotedSharesAsset(idx: Int): UserAnswers =
      ua.set(
        UnquotedSharesCompanyNamePage(idx),
        s"UnquotedCompany-${idx + 1}"
      ).success.value
        .set(UnquotedSharesValuePage(idx), BigDecimal(500 + idx * 125))
        .success.value
        .set(UnquotedSharesNumberPage(idx), 600 + idx * 150)
        .success.value
        .set(UnquotedSharesClassPage(idx), "B")
        .success.value

  }
}

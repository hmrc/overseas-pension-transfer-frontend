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
import models.address.{Country, QROPSAddress}
import models.{PersonName, SchemeManagerType, UserAnswers}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import pages.qropsSchemeManagerDetails._

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
  }
}

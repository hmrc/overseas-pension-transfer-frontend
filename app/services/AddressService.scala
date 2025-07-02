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

package services

import connectors.{AddressLookupConnector, AddressLookupErrorResponse, AddressLookupSuccessResponse}
import forms.memberDetails.MembersCurrentAddressFormData
import forms.qropsDetails.QROPSAddressFormData
import forms.qropsSchemeManagerDetails.SchemeManagersAddressFormData
import forms.transferDetails.PropertyAddressFormData
import models.UserAnswers
import models.address._
import pages.memberDetails.{MembersLastUkAddressLookupPage, MembersLastUkAddressSelectPage}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddressService @Inject() (
    countryService: CountryService,
    addressLookupConnector: AddressLookupConnector
  )(implicit ec: ExecutionContext
  ) {

  private def buildBaseAddress(
      line1: String,
      line2: String,
      line3: Option[String],
      line4: Option[String],
      line5: Option[String],
      countryCode: String,
      postcode: Option[String],
      poBox: Option[String]
    ): Option[BaseAddress] =
    countryService.find(countryCode).map { country =>
      BaseAddress(
        line1    = line1,
        line2    = line2,
        line3    = line3,
        line4    = line4,
        line5    = line5,
        country  = country,
        postcode = postcode,
        poBox    = poBox
      )
    }

  def propertyAddress(data: PropertyAddressFormData): Option[PropertyAddress] =
    buildBaseAddress(
      data.addressLine1,
      data.addressLine2,
      data.addressLine3,
      data.addressLine4,
      None,
      data.countryCode,
      data.postcode,
      None
    ).map(PropertyAddress(_))

  def schemeManagersAddress(data: SchemeManagersAddressFormData): Option[SchemeManagersAddress] =
    buildBaseAddress(
      data.addressLine1,
      data.addressLine2,
      data.addressLine3,
      data.addressLine4,
      data.addressLine5,
      data.countryCode,
      None,
      None
    ).map(SchemeManagersAddress(_))

  def qropsAddress(data: QROPSAddressFormData): Option[QROPSAddress] =
    buildBaseAddress(
      data.addressLine1,
      data.addressLine2,
      data.addressLine3,
      data.addressLine4,
      data.addressLine5,
      data.countryCode,
      None,
      None
    ).map(QROPSAddress(_))

  def membersCurrentAddress(data: MembersCurrentAddressFormData): Option[MembersCurrentAddress] =
    buildBaseAddress(
      data.addressLine1,
      data.addressLine2,
      data.addressLine3,
      data.addressLine4,
      None,
      data.countryCode,
      data.postcode,
      data.poBox
    ).map(MembersCurrentAddress(_))

  def membersLastUkAddressLookup(postcode: String)(implicit hc: HeaderCarrier): Future[Option[FoundAddressResponse]] =
    addressLookupConnector.lookup(postcode).map {
      case AddressLookupSuccessResponse(searched, rs) =>
        Some(FoundAddressResponse.fromRecordSet(searched, rs))
      case AddressLookupErrorResponse(_)              =>
        None
    }

  def addressIds(found: FoundAddressSet): Seq[String] =
    found.addresses.map(_.id)

  def findAddressById(found: FoundAddressSet, selectedId: String): Option[FoundAddress] =
    found.addresses.find(_.id == selectedId)

  def clearAddressLookups(answers: UserAnswers): Future[UserAnswers] =
    Future.fromTry(
      answers
        .remove(MembersLastUkAddressLookupPage)
        .flatMap(_.remove(MembersLastUkAddressSelectPage))
    )
}

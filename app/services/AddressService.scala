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

class AddressService @Inject() (countryService: CountryService, addressLookupConnector: AddressLookupConnector)(implicit ex: ExecutionContext) {

  def propertyAddress(data: PropertyAddressFormData): Option[PropertyAddress] =
    countryService.find(data.countryCode).map { country =>
      PropertyAddress(
        data.addressLine1,
        data.addressLine2,
        data.addressLine3,
        data.addressLine4,
        country,
        data.postcode
      )
    }

  def schemeManagersAddress(data: SchemeManagersAddressFormData): Option[SchemeManagersAddress] =
    countryService.find(data.countryCode).map { country =>
      SchemeManagersAddress(
        data.addressLine1,
        data.addressLine2,
        data.addressLine3,
        data.addressLine4,
        data.addressLine5,
        country
      )
    }

  def qropsAddress(data: QROPSAddressFormData): Option[QROPSAddress] =
    countryService.find(data.countryCode).map { country =>
      QROPSAddress(
        data.addressLine1,
        data.addressLine2,
        data.addressLine3,
        data.addressLine4,
        data.addressLine5,
        country
      )
    }

  def membersCurrentAddress(data: MembersCurrentAddressFormData): Option[MembersCurrentAddress] =
    countryService.find(data.countryCode).map { country =>
      MembersCurrentAddress(
        data.addressLine1,
        data.addressLine2,
        data.addressLine3,
        data.addressLine4,
        country,
        data.postcode,
        data.poBox
      )
    }

  def membersLastUkAddressLookup(postcode: String)(implicit hc: HeaderCarrier): Future[Option[FoundAddressResponse]] =
    addressLookupConnector.lookup(postcode).map {
      case AddressLookupSuccessResponse(searched, rs) =>
        Some(FoundAddressResponse.fromRecordSet(searched, rs))
      case AddressLookupErrorResponse(_)              =>
        None
    }

  def addressIds(found: FoundAddressSet): Seq[String] = found.addresses.map(_.id)

  def findAddressById(found: FoundAddressSet, selectedId: String): Option[FoundAddress] =
    found.addresses.find(_.id == selectedId)

  def clearAddressLookups(answers: UserAnswers): Future[UserAnswers] =
    Future.fromTry(answers
      .remove(MembersLastUkAddressLookupPage)
      .flatMap(_.remove(MembersLastUkAddressSelectPage)))

}

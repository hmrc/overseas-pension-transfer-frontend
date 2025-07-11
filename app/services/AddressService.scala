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

import connectors.AddressLookupConnector
import forms.memberDetails.MembersCurrentAddressFormData
import forms.qropsDetails.QROPSAddressFormData
import forms.qropsSchemeManagerDetails.SchemeManagersAddressFormData
import forms.transferDetails.PropertyAddressFormData
import models.UserAnswers
import models.address._
import models.responses.{AddressLookupErrorResponse, AddressLookupSuccessResponse}
import pages.memberDetails.{MembersLastUkAddressLookupPage, MembersLastUkAddressSelectPage}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddressService @Inject() (
    countryService: CountryService,
    addressLookupConnector: AddressLookupConnector
  )(implicit ex: ExecutionContext
  ) {

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

  def membersLastUkAddressLookup(postcode: String)(implicit hc: HeaderCarrier): Future[Option[AddressLookupResult]] =
    addressLookupConnector.lookup(postcode).map {
      case AddressLookupSuccessResponse(searched, records) =>
        if (records.nonEmpty) {
          Some(AddressRecords(searched, records))
        } else {
          Some(NoAddressFound(searched))
        }
      case AddressLookupErrorResponse(_)                   =>
        None
    }

  def addressIds(records: Seq[AddressRecord]): Seq[String] =
    records.map(_.id)

  def findAddressById(records: Seq[AddressRecord], selectedId: String): Option[AddressRecord] =
    records.find(_.id == selectedId)

  def clearAddressLookups(answers: UserAnswers): Future[UserAnswers] =
    Future.fromTry(
      answers
        .remove(MembersLastUkAddressLookupPage)
        .flatMap(_.remove(MembersLastUkAddressSelectPage))
    )
}

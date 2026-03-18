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

import forms.memberDetails.MembersCurrentAddressFormData
import forms.qropsDetails.QROPSAddressFormData
import forms.qropsSchemeManagerDetails.SchemeManagersAddressFormData
import forms.transferDetails.assetsMiniJourneys.property.PropertyAddressFormDataTrait
import models.address._

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AddressService @Inject() (
    countryService: CountryService
  )(implicit ex: ExecutionContext
  ) {

  def propertyAddress(data: PropertyAddressFormDataTrait): Option[PropertyAddress] =
    countryService.findByCode(data.countryCode).map { country =>
      PropertyAddress(
        data.addressLine1,
        data.addressLine2,
        data.town,
        data.county,
        data.poBoxNumber,
        country,
        data.postcode
      )
    }

  def schemeManagersAddress(data: SchemeManagersAddressFormData): Option[SchemeManagersAddress] =
    countryService.findByCode(data.countryCode).map { country =>
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
    countryService.findByCode(data.countryCode).map { country =>
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
    countryService.findByCode(data.countryCode).map { country =>
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
}

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

import base.AddressBase
import connectors.{AddressLookupConnector, AddressLookupErrorResponse, AddressLookupSuccessResponse}
import forms.memberDetails.MembersCurrentAddressFormData
import forms.qropsDetails.QROPSAddressFormData
import forms.qropsSchemeManagerDetails.SchemeManagersAddressFormData
import models.address._
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AddressServiceSpec
    extends AnyFreeSpec
    with AddressBase
    with MockitoSugar
    with ScalaFutures {

  implicit private val hc: HeaderCarrier = HeaderCarrier()

  private val mockCountryService         = mock[CountryService]
  private val mockAddressLookupConnector = mock[AddressLookupConnector]

  private val service = new AddressService(mockCountryService, mockAddressLookupConnector)

  ".schemeManagersAddress" - {
    val formData = SchemeManagersAddressFormData(
      addressLine1 = schemeManagersAddress.addressLine1,
      addressLine2 = schemeManagersAddress.addressLine2,
      addressLine3 = schemeManagersAddress.addressLine3,
      addressLine4 = schemeManagersAddress.addressLine4,
      addressLine5 = schemeManagersAddress.addressLine5,
      countryCode  = schemeManagersAddress.countryCode.code
    )

    "must construct a SchemeManagersAddress when the country exists" in {
      when(mockCountryService.find(Countries.UK.code)).thenReturn(Some(Countries.UK))

      val result = service.schemeManagersAddress(formData).value

      result mustBe schemeManagersAddress
    }

    "must return None when the country code is unknown" in {
      when(mockCountryService.find(anyString())).thenReturn(None)

      val formDataNoCountry = formData.copy(countryCode = "XX")
      service.schemeManagersAddress(formDataNoCountry) mustBe None
    }
  }

  ".qropsAddress" - {

    "must construct a QROPSAddress when the country exists" in {
      when(mockCountryService.find(Countries.UK.code)).thenReturn(Some(Countries.UK))

      val formData = QROPSAddressFormData(
        addressLine1 = qropsAddress.addressLine1,
        addressLine2 = qropsAddress.addressLine2,
        addressLine3 = qropsAddress.addressLine3,
        addressLine4 = qropsAddress.addressLine4,
        addressLine5 = qropsAddress.addressLine5,
        countryCode  = qropsAddress.countryCode.code
      )

      service.qropsAddress(formData).value mustBe qropsAddress
    }
  }

  ".membersCurrentAddress" - {

    "must map the form data (including postcode and PO box)" in {
      when(mockCountryService.find(Countries.UK.code)).thenReturn(Some(Countries.UK))

      val formData = MembersCurrentAddressFormData(
        addressLine1 = membersCurrentAddress.addressLine1,
        addressLine2 = membersCurrentAddress.addressLine2,
        addressLine3 = membersCurrentAddress.addressLine3,
        addressLine4 = membersCurrentAddress.addressLine4,
        countryCode  = qropsAddress.countryCode.code,
        postcode     = membersCurrentAddress.postcode,
        poBox        = membersCurrentAddress.poBox
      )

      service.membersCurrentAddress(formData).value mustBe membersCurrentAddress
    }
  }

  ".membersLastUkAddressLookup" - {

    "must return Some(FoundAddressSet) when the connector responds successfully" in {
      val successResponse = AddressLookupSuccessResponse(connectorPostcode, recordSet)

      when(mockAddressLookupConnector.lookup(connectorPostcode))
        .thenReturn(Future.successful(successResponse))

      whenReady(service.membersLastUkAddressLookup(connectorPostcode)) { maybe =>
        maybe.value mustBe FoundAddressResponse.fromRecordSet(connectorPostcode, recordSet)
      }
    }

    "must return None when the connector returns an error response" in {
      when(mockAddressLookupConnector.lookup(connectorPostcode))
        .thenReturn(Future.successful(AddressLookupErrorResponse(new RuntimeException("boom"))))

      whenReady(service.membersLastUkAddressLookup(connectorPostcode)) { maybe =>
        maybe mustBe None
      }
    }
  }

  ".addressIds" - {

    "must return the ids from the FoundAddressSet in order" in {
      service.addressIds(foundAddresses) mustBe validIds
    }
  }

  ".findAddressById" - {

    "must return Some(address) when the id exists" in {
      service.findAddressById(foundAddresses, selectedAddress.id).value mustBe selectedAddress
    }

    "must return None when the id does not exist" in {
      service.findAddressById(foundAddresses, "missing") mustBe None
    }
  }
}

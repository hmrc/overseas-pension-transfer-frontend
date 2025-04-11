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

package base

import models.address._
import models.UserAnswers
import pages.{MembersLastUkAddressLookupPage, MembersLastUkAddressSelectPage}

trait AddressBase {

  val connectorPostcode = "BB00 1BB"

  val recordSet: RecordSet =
    RecordSet(
      Seq(
        AddressRecord(
          id                   = "GB200000698110",
          uprn                 = Some(200000698110L),
          parentUprn           = Some(200000698110L),
          usrn                 = Some(200000698110L),
          organisation         = Some("Test Organisation"),
          address              = RawAddress(
            lines       = List("2 Test Close"),
            town        = "Test Town",
            postcode    = "BB00 1BB",
            subdivision = Some(Subdivision(
              code = "GB-ENG",
              name = "England"
            )),
            country     = Country(
              code = "GB",
              name = "United Kingdom"
            )
          ),
          language             = "en",
          localCustodian       = Some(
            LocalCustodian(
              code = 1760,
              name = "Test Valley"
            )
          ),
          location             = Some(Seq(BigDecimal(-1.234), BigDecimal(50.678))),
          blpuState            = None,
          logicalState         = None,
          streetClassification = None,
          administrativeArea   = Some("Some Area"),
          poBox                = Some("1234")
        ),
        AddressRecord(
          id                   = "GB200000708497",
          uprn                 = Some(200000708497L),
          parentUprn           = Some(200000708497L),
          usrn                 = Some(200000708497L),
          organisation         = Some("Another Organisation"),
          address              = RawAddress(
            lines       = List("4 Test Close"),
            town        = "Test Town",
            postcode    = "BB00 1BB",
            subdivision = Some(Subdivision(
              code = "GB-ENG",
              name = "England"
            )),
            country     = Country(
              code = "GB",
              name = "United Kingdom"
            )
          ),
          language             = "en",
          localCustodian       = Some(
            LocalCustodian(
              code = 1760,
              name = "Test Valley"
            )
          ),
          location             = Some(Seq(BigDecimal(-1.234), BigDecimal(50.678))),
          blpuState            = None,
          logicalState         = None,
          streetClassification = None,
          administrativeArea   = Some("Some Other Area"),
          poBox                = Some("5678")
        )
      )
    )

  val foundAddresses: FoundAddressSet =
    FoundAddressSet(
      searchedPostcode = "ZZ1 1ZZ",
      addresses        =
        Seq(
          FoundAddress(
            id      = "GB990091234514",
            address = MembersLookupLastUkAddress(
              line1    = "2 Other Place",
              line2    = "Some District",
              line3    = None,
              line4    = None,
              postcode = Some("ZZ1 1ZZ"),
              country  = Countries.UK,
              poBox    = None
            )
          ),
          FoundAddress(
            id      = "GB990091234515",
            address = MembersLookupLastUkAddress(
              line1    = "3 Other Place",
              line2    = "Some District",
              line3    = None,
              line4    = None,
              postcode = Some("ZZ1 1ZZ"),
              country  = Countries.UK,
              poBox    = None
            )
          )
        )
    )

  val membersCurrentAddress: MembersCurrentAddress = MembersCurrentAddress(
    addressLine1 = "2 Other Place",
    addressLine2 = "Some District",
    addressLine3 = None,
    addressLine4 = None,
    postcode     = Some("ZZ1 1ZZ"),
    country      = Countries.UK,
    poBox        = None
  )

  val qropsAddress: QROPSAddress = QROPSAddress(
    addressLine1 = "2 Other Place",
    addressLine2 = "Some District",
    addressLine3 = None,
    addressLine4 = None,
    addressLine5 = None,
    country      = Countries.UK
  )

  val addressFoundUserAnswers: UserAnswers = UserAnswers("id").set(MembersLastUkAddressLookupPage, foundAddresses).get

  val selectedAddress: FoundAddress = foundAddresses.addresses.head

  val addressSelectedUserAnswers: UserAnswers = addressFoundUserAnswers.set(MembersLastUkAddressSelectPage, selectedAddress).get

  val validIds: Seq[String] = foundAddresses.addresses.map(_.id)

  val noAddressFound: NoAddressFound = NoAddressFound(searchedPostcode = "ZZ1 1ZZ")

  val noAddressFoundUserAnswers: UserAnswers = UserAnswers("id").set(MembersLastUkAddressLookupPage, noAddressFound).get

}

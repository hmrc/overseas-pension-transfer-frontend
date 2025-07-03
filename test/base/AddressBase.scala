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

import models.UserAnswers
import models.address._
import pages.memberDetails.{MembersLastUkAddressLookupPage, MembersLastUkAddressSelectPage}

trait AddressBase extends SpecBase {

  val connectorPostcode = "BB00 1BB"

  val addressRecordList: Seq[AddressRecord] =
    Seq(
      AddressRecord(
        id                   = "GB990091234514",
        uprn                 = None,
        parentUprn           = None,
        usrn                 = None,
        organisation         = Some("Fake Org Ltd"),
        address              = RawAddress(
          lines       = List("2 Other Place", "Some District"),
          town        = "Faketown",
          postcode    = "BB00 1BB",
          subdivision = Some(Subdivision("ENG", "England")),
          country     = Countries.UK
        ),
        language             = "en",
        localCustodian       = Some(LocalCustodian(1234, "Fake Local Authority")),
        location             = Some(Seq(BigDecimal("51.5074"), BigDecimal("-0.1278"))),
        blpuState            = Some("1"),
        logicalState         = Some("1"),
        streetClassification = Some("RD"),
        administrativeArea   = Some("Fake County"),
        poBox                = None
      ),
      AddressRecord(
        id                   = "GB990091234515",
        uprn                 = Some(12345678902L),
        parentUprn           = None,
        usrn                 = Some(987654322L),
        organisation         = None,
        address              = RawAddress(
          lines       = List("3 Other Place", "Some District"),
          town        = "Faketown",
          postcode    = "BB00 1BB",
          subdivision = Some(Subdivision("ENG", "England")),
          country     = Countries.UK
        ),
        language             = "en",
        localCustodian       = Some(LocalCustodian(1234, "Fake Local Authority")),
        location             = Some(Seq(BigDecimal("51.5075"), BigDecimal("-0.1277"))),
        blpuState            = Some("1"),
        logicalState         = Some("1"),
        streetClassification = Some("RD"),
        administrativeArea   = Some("Fake County"),
        poBox                = None
      )
    )

  val addressRecords: AddressRecords = AddressRecords(postcode = "BB00 1BB", records = addressRecordList)

  val validIds: Seq[String] = addressRecordList.map(_.id)

  val selectedRecord: AddressRecord = addressRecordList.head

  val addressFoundUserAnswers: UserAnswers =
    userAnswersMemberNameQtNumber
      .set(MembersLastUkAddressLookupPage, addressRecords).success.value

  val addressSelectedUserAnswers: UserAnswers =
    addressFoundUserAnswers
      .set(MembersLastUkAddressSelectPage, MembersLookupLastUkAddress.fromAddressRecord(selectedRecord)).success.value

  val noAddressFound: NoAddressFound = NoAddressFound(postcode = "AB1 1CD")

  val noAddressFoundUserAnswers: UserAnswers =
    userAnswersMemberNameQtNumber
      .set(MembersLastUkAddressLookupPage, noAddressFound).success.value

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

  val schemeManagersAddress: SchemeManagersAddress = SchemeManagersAddress(
    addressLine1 = "2 Other Place",
    addressLine2 = "Some District",
    addressLine3 = None,
    addressLine4 = None,
    addressLine5 = None,
    country      = Countries.UK
  )

  val propertyAddress: PropertyAddress = PropertyAddress(
    addressLine1 = "2 Other Place",
    addressLine2 = "Some District",
    addressLine3 = None,
    addressLine4 = None,
    postcode     = Some("ZZ1 1ZZ"),
    country      = Countries.UK
  )
}

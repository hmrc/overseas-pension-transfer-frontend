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
import pages.{MemberSelectLastUkAddressPage, MembersLastUkAddressLookupPage}

trait AddressBase {

  val foundAddresses: FoundAddressSet      =
    FoundAddressSet(
      searchedPostcode = "ZZ1 1ZZ",
      addresses        =
        Seq(
          FoundAddress(
            id      = "GB990091234514",
            address = MembersLookupLastUkAddress(
              line1      = "2 Other Place",
              line2      = Some("Some District"),
              line3      = None,
              townOrCity = Some("Anytown"),
              postcode   = Some("ZZ1 1ZZ"),
              country    = Some("United Kingdom")
            )
          ),
          FoundAddress(
            id      = "GB990091234515",
            address = MembersLookupLastUkAddress(
              line1      = "3 Other Place",
              line2      = Some("Some District"),
              line3      = None,
              townOrCity = Some("Anytown"),
              postcode   = Some("ZZ1 1ZZ"),
              country    = Some("United Kingdom")
            )
          )
        )
    )
  val addressFoundUserAnswers: UserAnswers = UserAnswers("id").set(MembersLastUkAddressLookupPage, foundAddresses).get

  val selectedAddress: FoundAddress = foundAddresses.addresses.head

  val addressSelectedUserAnswers: UserAnswers = addressFoundUserAnswers.set(MemberSelectLastUkAddressPage, selectedAddress).get

  val validIds: Seq[String] = foundAddresses.addresses.map(_.id)

  val noAddressFound: NoAddressFound = NoAddressFound(searchedPostcode = "ZZ1 1ZZ")

  val noAddressFoundUserAnswers: UserAnswers = UserAnswers("id").set(MembersLastUkAddressLookupPage, noAddressFound).get

}

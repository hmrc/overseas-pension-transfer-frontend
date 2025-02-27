/*
 * Copyright 2024 HM Revenue & Customs
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

package generators

import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {

  implicit lazy val arbitrarySchemeManagersName: Arbitrary[SchemeManagersName] =
    Arbitrary {
      for {
        firstName <- arbitrary[String]
        lastName <- arbitrary[String]
      } yield SchemeManagersName(firstName, lastName)
    }

  implicit lazy val arbitraryMembersCurrentAddress: Arbitrary[MembersCurrentAddress] =
    Arbitrary {
      for {
        addressLine1 <- arbitrary[String]
        addressLine2 <- arbitrary[String]
        addressLine3 <- arbitrary[String]
        city         <- arbitrary[String]
        country      <- arbitrary[String]
        postcode     <- arbitrary[String]
      } yield MembersCurrentAddress(addressLine1, addressLine2, Some(addressLine3), Some(city), Some(country), Some(postcode))
    }

  implicit lazy val arbitraryMemberName: Arbitrary[MemberName] =
    Arbitrary {
      for {
        memberFirstName <- arbitrary[String]
        memberLastName  <- arbitrary[String]
      } yield MemberName(memberFirstName, memberLastName)
    }
}

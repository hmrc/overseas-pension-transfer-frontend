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

  implicit lazy val arbitraryQROPSSchemeManagerIsIndividualOrOrg: Arbitrary[QROPSSchemeManagerIsIndividualOrOrg] =
    Arbitrary {
      Gen.oneOf(QROPSSchemeManagerIsIndividualOrOrg.values)
    }

  implicit lazy val arbitrarySchemeManagersName: Arbitrary[SchemeManagersName] =
    Arbitrary {
      for {
        firstName <- arbitrary[String]
        lastName  <- arbitrary[String]
      } yield SchemeManagersName(firstName, lastName)
    }

  implicit lazy val arbitraryMemberName: Arbitrary[PersonName] =
    Arbitrary {
      for {
        memberFirstName <- arbitrary[String]
        memberLastName  <- arbitrary[String]
      } yield PersonName(memberFirstName, memberLastName)
    }
}

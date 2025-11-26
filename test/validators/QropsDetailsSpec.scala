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

package validators

import base.AddressBase
import cats.data.Validated.{Invalid, Valid}
import models._
import models.address.Country
import models.transferJourneys.QropsDetails
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.qropsDetails._
import uaOps.UAOps.QropsAnswersOps

final class QropsDetailsSpec extends AnyFreeSpec with Matchers with OptionValues with AddressBase {

  private val country: Country =
    Country(
      code = "AD",
      name = "Andorra"
    )

  private val name         = "Acme Pension Scheme"
  private val ref          = "Q1234567"
  private val otherCountry = "Narnia"

  "QropsDetailsValidator.fromUserAnswers" - {

    "must succeed when name, reference, address and (country only) are provided" in {
      val ua = emptyUserAnswers.withName(name).withRef(ref).withAddress(qropsAddress).withCountry(country)

      QropsDetailsValidator.fromUserAnswers(ua) match {
        case Valid(details) =>
          details mustBe QropsDetails(
            qropsName         = name,
            qropsReference    = ref,
            qropsAddress      = qropsAddress,
            qropsCountry      = Some(country),
            qropsOtherCountry = None
          )

        case Invalid(errs) =>
          fail(s"Expected Valid, but got Invalid with errors: $errs")
      }
    }

    "must succeed when name, reference, address, country IS 'Other' and otherCountry are provided" in {
      val ua = emptyUserAnswers.withName(name).withRef(ref).withAddress(qropsAddress).withCountry(Country("ZZ", "Other")).withOtherCountry(otherCountry)

      QropsDetailsValidator.fromUserAnswers(ua) match {
        case Valid(details) =>
          details mustBe QropsDetails(
            qropsName         = name,
            qropsReference    = ref,
            qropsAddress      = qropsAddress,
            qropsCountry      = Some(Country("ZZ", "Other")),
            qropsOtherCountry = Some(otherCountry)
          )

        case Invalid(errs) =>
          fail(s"Expected Valid, but got Invalid with errors: $errs")
      }
    }

    "must fail (and accumulate) when both country and otherCountry are present and country is NOT 'Other'" in {
      val ua = emptyUserAnswers.withName(name).withRef(ref).withAddress(qropsAddress).withCountry(country).withOtherCountry(otherCountry)

      val res = QropsDetailsValidator.fromUserAnswers(ua)

      res match {
        case Invalid(nec) =>
          nec.toNonEmptyList.toList must have size 1
          nec.toNonEmptyList.toList.head mustBe GenericError("Cannot have valid payload value of QROPS Country is not 'Other'")

        case Valid(v) =>
          fail(s"Expected Invalid with 1 GenericError, but got Valid: $v")
      }
    }

    "must fail (and accumulate) when country is NOT present and otherCountry is present" in {
      val ua = emptyUserAnswers.withName(name).withRef(ref).withAddress(qropsAddress).withOtherCountry(otherCountry)

      val res = QropsDetailsValidator.fromUserAnswers(ua)

      res match {
        case Invalid(nec) =>
          nec.toNonEmptyList.toList must have size 2
          nec.toNonEmptyList.toList.head mustBe DataMissingError(QROPSCountryPage)
          nec.toNonEmptyList.toList(1) mustBe GenericError("Cannot have a value for other Country where value of QROPS Country is None")

        case Valid(v) =>
          fail(s"Expected Invalid with 1 GenericError and DataMissingError, but got Valid: $v")
      }
    }

    "must fail with DataMissingError(QROPSCountryPage) and DataMissingError(QROPSOtherCountryPage)" +
      " when country and otherCountry aren't provided (others present)" in {
        val ua = emptyUserAnswers.withName(name).withRef(ref).withAddress(qropsAddress)

        val res = QropsDetailsValidator.fromUserAnswers(ua)

        res match {
          case Invalid(nec) =>
            nec.toNonEmptyList.toList must contain(DataMissingError(QROPSCountryPage))
            nec.toNonEmptyList.toList must contain(DataMissingError(QROPSOtherCountryPage))
            nec.length mustBe 2

          case Valid(v) =>
            fail(s"Expected Invalid due to missing country pair, but got Valid: $v")
        }
      }

    "must fail (and accumulate) when all the data is missing" in {
      val res = QropsDetailsValidator.fromUserAnswers(emptyUserAnswers)

      res match {
        case Invalid(nec) =>
          val errors = nec.toNonEmptyList.toList

          errors must contain(DataMissingError(QROPSNamePage))
          errors must contain(DataMissingError(QROPSReferencePage))
          errors must contain(DataMissingError(QROPSAddressPage))
          errors must contain(DataMissingError(QROPSCountryPage))
          errors must contain(DataMissingError(QROPSOtherCountryPage))
          errors must have size 5

        case Valid(v) =>
          fail(s"Expected Invalid with 5 DataMissingErrors, but got Valid: $v")
      }
    }
  }
}

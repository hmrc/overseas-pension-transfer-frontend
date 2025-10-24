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
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.qropsSchemeManagerDetails._
import uaOps.UAOps.SchemeManagerAnswersOps

final class SchemeManagerDetailsValidatorSpec
    extends AnyFreeSpec
    with Matchers
    with OptionValues with AddressBase {

  private val person: PersonName     = PersonName("Jane", "Doe")
  private val orgName: String        = "Acme Pensions Ltd"
  private val orgContact: PersonName = PersonName("Alex", "Smith")
  private val email: String          = "blah@blah.com"
  private val phoneNo: String        = "0123456789"

  private val V = SchemeManagerDetailsValidator

  "validateSchemeManagerType" - {

    "must succeed with Individual when provided" in {
      val ua =
        emptyUserAnswers
          .withSchemeManagerType(SchemeManagerType.Individual)

      V.validateSchemeManagerType(ua) match {
        case Valid(res)   => res mustBe SchemeManagerType.Individual
        case Invalid(err) => fail(s"Expected Valid(Individual), got Invalid: $err")
      }
    }

    "must succeed with Organisation when provided" in {
      val ua =
        emptyUserAnswers
          .withSchemeManagerType(SchemeManagerType.Organisation)

      V.validateSchemeManagerType(ua) match {
        case Valid(res)   => res mustBe SchemeManagerType.Organisation
        case Invalid(err) => fail(s"Expected Valid(Organisation), got Invalid: $err")
      }
    }

    "must fail with DataMissingError(SchemeManagerTypePage) when the type is missing" in {
      val ua = emptyUserAnswers

      V.validateSchemeManagerType(ua) match {
        case Invalid(nec) =>
          nec.toNonEmptyList.toList must contain only DataMissingError(SchemeManagerTypePage)
        case Valid(v)     =>
          fail(s"Expected Invalid(DataMissingError type), got Valid: $v")
      }
    }
  }

  "validateSchemeManagersName" - {

    "must fail with DataMissingError(SchemeManagerTypePage) when type is missing" in {
      val ua = emptyUserAnswers

      V.validateSchemeManagersName(ua) match {
        case Invalid(nec) =>
          nec.toNonEmptyList.toList must contain only DataMissingError(SchemeManagerTypePage)
        case Valid(v)     =>
          fail(s"Expected Invalid(DataMissingError type), got Valid: $v")
      }
    }

    // The idea is that org would fail if indi is present and indi would fail if org is present, so this scenario would fail
    // but it isn't tested here it is tested in the org test
    "must succeed with Some(person) when type = Individual and individual name is present (ignores any org fields)" in {
      val ua =
        emptyUserAnswers
          .withSchemeManagerType(SchemeManagerType.Individual)
          .withSchemeManagersName(person)
          .withSchemeManagerOrganisationName(orgName)
          .withSchemeManagerOrgContact(orgContact)

      V.validateSchemeManagersName(ua) match {
        case Valid(res)   => res.value mustBe person
        case Invalid(err) => fail(s"Expected Valid(Some(person)), got Invalid: $err")
      }
    }

    "must fail with DataMissingError(SchemeManagersNamePage) when type = Individual and individual name is missing" in {
      val ua =
        emptyUserAnswers
          .withSchemeManagerType(SchemeManagerType.Individual)

      V.validateSchemeManagersName(ua) match {
        case Invalid(nec) =>
          nec.toNonEmptyList.toList must contain only DataMissingError(SchemeManagersNamePage)
        case Valid(v)     =>
          fail(s"Expected Invalid(DataMissingError name), got Valid: $v")
      }
    }

    "must return None when type = Organisation and individual name is absent" in {
      val ua =
        emptyUserAnswers
          .withSchemeManagerType(SchemeManagerType.Organisation)
          .withSchemeManagerOrganisationName(orgName)
          .withSchemeManagerOrgContact(orgContact)

      V.validateSchemeManagersName(ua) match {
        case Valid(res)   => res mustBe None
        case Invalid(err) => fail(s"Expected Valid(None), got Invalid: $err")
      }
    }

    "must fail with GenericError('must be absent') when type = Organisation and individual name is present" in {
      val ua =
        emptyUserAnswers
          .withSchemeManagerType(SchemeManagerType.Organisation)
          .withSchemeManagersName(person)

      V.validateSchemeManagersName(ua) match {
        case Invalid(nec) =>
          nec.toNonEmptyList.toList must contain only GenericError(
            "Individual name must be absent when manager type is org"
          )
        case Valid(v)     =>
          fail(s"Expected Invalid(must be absent), got Valid: $v")
      }
    }
  }

  "validateSchemeManagersOrgName" - {

    "must fail with DataMissingError(SchemeManagerTypePage) when type is missing" in {
      val ua = emptyUserAnswers

      V.validateSchemeManagersOrgName(ua) match {
        case Invalid(nec) =>
          nec.toNonEmptyList.toList must contain only DataMissingError(SchemeManagerTypePage)
        case Valid(v)     =>
          fail(s"Expected Invalid(DataMissingError type), got Valid: $v")
      }
    }

    "must succeed with Some(orgName) when type = Organisation and org name is present (contact may be absent here)" in {
      val ua =
        emptyUserAnswers
          .withSchemeManagerType(SchemeManagerType.Organisation)
          .withSchemeManagerOrganisationName(orgName)

      V.validateSchemeManagersOrgName(ua) match {
        case Valid(res)   => res.value mustBe orgName
        case Invalid(err) => fail(s"Expected Valid(Some(orgName)), got Invalid: $err")
      }
    }

    "must fail with DataMissingError(SchemeManagerOrganisationNamePage) when type = Organisation and org name is missing" in {
      val ua =
        emptyUserAnswers
          .withSchemeManagerType(SchemeManagerType.Organisation)

      V.validateSchemeManagersOrgName(ua) match {
        case Invalid(nec) =>
          nec.toNonEmptyList.toList must contain only DataMissingError(SchemeManagerOrganisationNamePage)
        case Valid(v)     =>
          fail(s"Expected Invalid(DataMissingError org name), got Valid: $v")
      }
    }

    "must return None when type = Individual and org name is absent" in {
      val ua =
        emptyUserAnswers
          .withSchemeManagerType(SchemeManagerType.Individual)
          .withSchemeManagersName(person)

      V.validateSchemeManagersOrgName(ua) match {
        case Valid(res)   => res mustBe None
        case Invalid(err) => fail(s"Expected Valid(None), got Invalid: $err")
      }
    }

    "must fail when type = Individual and individual name is present alongside org name and org contact" in {
      val ua =
        emptyUserAnswers
          .withSchemeManagerType(SchemeManagerType.Individual)
          .withSchemeManagersName(person)
          .withSchemeManagerOrganisationName(orgName)
          .withSchemeManagerOrgContact(orgContact)

      V.validateSchemeManagersOrgName(ua) match {
        case Invalid(nec) => nec.toNonEmptyList.toList must contain only GenericError(
            "Org name must be absent when manager type is individual"
          )
        case Valid(v)     => fail(s"Expected Invalid(must be absent), got Valid: $v")

      }
    }
  }

  "validateSchemeOrgContact" - {

    "must fail with DataMissingError(SchemeManagerTypePage) when type is missing" in {
      val ua = emptyUserAnswers

      V.validateSchemeOrgContact(ua) match {
        case Invalid(nec) =>
          nec.toNonEmptyList.toList must contain only DataMissingError(SchemeManagerTypePage)
        case Valid(v)     =>
          fail(s"Expected Invalid(DataMissingError type), got Valid: $v")
      }
    }

    "must succeed with Some(contact) when type = Organisation and contact is present (org name may be absent here)" in {
      val ua =
        emptyUserAnswers
          .withSchemeManagerType(SchemeManagerType.Organisation)
          .withSchemeManagerOrgContact(orgContact)

      V.validateSchemeOrgContact(ua) match {
        case Valid(res)   => res.value mustBe orgContact
        case Invalid(err) => fail(s"Expected Valid(Some(contact)), got Invalid: $err")
      }
    }

    "must fail with DataMissingError(SchemeManagerOrgIndividualNamePage) when type = Organisation and contact is missing" in {
      val ua =
        emptyUserAnswers
          .withSchemeManagerType(SchemeManagerType.Organisation)

      V.validateSchemeOrgContact(ua) match {
        case Invalid(nec) =>
          nec.toNonEmptyList.toList must contain only DataMissingError(SchemeManagerOrgIndividualNamePage)
        case Valid(v)     =>
          fail(s"Expected Invalid(DataMissingError contact), got Valid: $v")
      }
    }

    "must return None when type = Individual and contact is absent (correctly absent)" in {
      val ua =
        emptyUserAnswers
          .withSchemeManagerType(SchemeManagerType.Individual)
          .withSchemeManagersName(person)

      V.validateSchemeOrgContact(ua) match {
        case Valid(res)   => res mustBe None
        case Invalid(err) => fail(s"Expected Valid(None), got Invalid: $err")
      }
    }

    "must fail with GenericError('must be absent') when type = Individual and contact is present" in {
      val ua =
        emptyUserAnswers
          .withSchemeManagerType(SchemeManagerType.Individual)
          .withSchemeManagerOrgContact(orgContact)

      V.validateSchemeOrgContact(ua) match {
        case Invalid(nec) =>
          nec.toNonEmptyList.toList must contain only GenericError(
            "Org contact must be absent when manager type is individual"
          )
        case Valid(v)     =>
          fail(s"Expected Invalid(must be absent), got Valid: $v")
      }
    }
  }
  "validateSchemeManagerAddress" - {

    "must succeed with address when provided" in {
      val ua =
        emptyUserAnswers
          .withAddress(schemeManagersAddress)

      V.validateSchemeManagersAddress(ua) match {
        case Valid(res)   => res mustBe schemeManagersAddress
        case Invalid(err) => fail(s"Expected Valid(address), got Invalid: $err")
      }
    }

    "must fail with DataMissingError(SchemeManagerTypePage) when the type is missing" in {
      val ua = emptyUserAnswers

      V.validateSchemeManagersAddress(ua) match {
        case Invalid(nec) =>
          nec.toNonEmptyList.toList must contain only DataMissingError(SchemeManagersAddressPage)
        case Valid(v)     =>
          fail(s"Expected Invalid(DataMissingError type), got Valid: $v")
      }
    }
  }

  "validateSchemeManagerEmail" - {

    "must succeed with email when provided" in {
      val ua =
        emptyUserAnswers
          .withSchemeManagersEmail(email)

      V.validateSchemeManagersEmail(ua) match {
        case Valid(res)   => res mustBe email
        case Invalid(err) => fail(s"Expected Valid(email), got Invalid: $err")
      }
    }

    "must fail with DataMissingError(SchemeManagerTypePage) when the type is missing" in {
      val ua = emptyUserAnswers

      V.validateSchemeManagersEmail(ua) match {
        case Invalid(nec) =>
          nec.toNonEmptyList.toList must contain only DataMissingError(SchemeManagersEmailPage)
        case Valid(v)     =>
          fail(s"Expected Invalid(DataMissingError type), got Valid: $v")
      }
    }
  }

  "validateSchemeManagersPhoneNo" - {

    "must succeed with phone no when provided" in {
      val ua =
        emptyUserAnswers
          .withSchemeManagersPhoneNo(phoneNo)

      V.validateSchemeManagersPhoneNo(ua) match {
        case Valid(res)   => res mustBe phoneNo
        case Invalid(err) => fail(s"Expected Valid(phoneNo), got Invalid: $err")
      }
    }

    "must fail with DataMissingError(SchemeManagerTypePage) when the type is missing" in {
      val ua = emptyUserAnswers

      V.validateSchemeManagersPhoneNo(ua) match {
        case Invalid(nec) =>
          nec.toNonEmptyList.toList must contain only DataMissingError(SchemeManagersContactPage)
        case Valid(v)     =>
          fail(s"Expected Invalid(DataMissingError type), got Valid: $v")
      }
    }
  }
}

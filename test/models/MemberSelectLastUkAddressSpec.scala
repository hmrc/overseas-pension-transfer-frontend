package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class MemberSelectLastUkAddressSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "MemberSelectLastUkAddress" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(MemberSelectLastUkAddress.values.toSeq)

      forAll(gen) {
        memberSelectLastUkAddress =>
          JsString(memberSelectLastUkAddress.toString).validate[MemberSelectLastUkAddress].asOpt.value mustEqual memberSelectLastUkAddress
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!MemberSelectLastUkAddress.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>
          JsString(invalidValue).validate[MemberSelectLastUkAddress] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(MemberSelectLastUkAddress.values.toSeq)

      forAll(gen) {
        memberSelectLastUkAddress =>
          Json.toJson(memberSelectLastUkAddress) mustEqual JsString(memberSelectLastUkAddress.toString)
      }
    }
  }
}

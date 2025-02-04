package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class MemberIsResidentSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "MemberIsResidentUK" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(MemberIsResidentUK.values.toSeq)

      forAll(gen) {
        memberIsResidentUk =>

          JsString(memberIsResidentUk.toString).validate[MemberIsResidentUK].asOpt.value mustEqual memberIsResidentUk
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!MemberIsResidentUK.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[MemberIsResidentUK] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(MemberIsResidentUK.values.toSeq)

      forAll(gen) {
        memberIsResidentUk =>

          Json.toJson(memberIsResidentUk) mustEqual JsString(memberIsResidentUk.toString)
      }
    }
  }
}

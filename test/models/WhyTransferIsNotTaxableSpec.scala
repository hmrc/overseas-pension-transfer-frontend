package models

import generators.ModelGenerators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class WhyTransferIsNotTaxableSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues with ModelGenerators {

  "WhyTransferIsNotTaxable" - {

    "must deserialise valid values" in {

      val gen = arbitrary[WhyTransferIsNotTaxable]

      forAll(gen) {
        whyTransferIsNotTaxable =>
          JsString(whyTransferIsNotTaxable.toString).validate[WhyTransferIsNotTaxable].asOpt.value mustEqual whyTransferIsNotTaxable
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!WhyTransferIsNotTaxable.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>
          JsString(invalidValue).validate[WhyTransferIsNotTaxable] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = arbitrary[WhyTransferIsNotTaxable]

      forAll(gen) {
        whyTransferIsNotTaxable =>
          Json.toJson(whyTransferIsNotTaxable) mustEqual JsString(whyTransferIsNotTaxable.toString)
      }
    }
  }
}

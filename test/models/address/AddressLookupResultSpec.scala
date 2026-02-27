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

package models.address

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues}
import play.api.libs.json._

class AddressLookupResultSpec extends AnyFreeSpec with Matchers with EitherValues with OptionValues {

  private def buildAddressRecord(id: String = "test-id-1", poBox: Option[String] = Some("PO Box 123")): AddressRecord = {
    AddressRecord(
      id      = id,
      address = RawAddress(
        lines    = List("123 Test Street", "Test Building"),
        town     = "Test Town",
        postcode = "TE1 1ST",
        country  = Country("GB", "United Kingdom")
      ),
      poBox   = poBox
    )
  }

  private def buildAddressRecordJson(id: String = "test-id-1", includePoBox: Boolean = true): JsObject = {
    val base = Json.obj(
      "id"      -> id,
      "address" -> Json.obj(
        "lines"    -> Json.arr("123 Test Street", "Test Building"),
        "town"     -> "Test Town",
        "postcode" -> "TE1 1ST",
        "country"  -> Json.obj("code" -> "GB", "name" -> "United Kingdom")
      )
    )
    if (includePoBox) base + ("poBox" -> JsString("PO Box 123")) else base
  }

  "AddressLookupResult JSON" - {

    "reads" - {

      "must read AddressRecords with non-empty records" in {
        val inputJson     = Json.obj("postcode" -> "TE1 1ST", "records" -> Json.arr(buildAddressRecordJson()))
        val expectedModel = AddressRecords("TE1 1ST", Seq(buildAddressRecord()))

        val result = inputJson.validate[AddressLookupResult].asEither.value
        result mustBe expectedModel
      }

      "must read NoAddressFound when records field is missing" in {
        val inputJson     = Json.obj("postcode" -> "TE1 1ST")
        val expectedModel = NoAddressFound("TE1 1ST")

        val result = inputJson.validate[AddressLookupResult].asEither.value
        result mustBe expectedModel
      }

      "must read NoAddressFound when records field is empty array" in {
        val inputJson     = Json.obj("postcode" -> "TE1 1ST", "records" -> Json.arr())
        val expectedModel = NoAddressFound("TE1 1ST")

        val result = inputJson.validate[AddressLookupResult].asEither.value
        result mustBe expectedModel
      }

      "must read NoAddressFound when records field is null" in {
        val inputJson     = Json.obj("postcode" -> "TE1 1ST", "records" -> JsNull)
        val expectedModel = NoAddressFound("TE1 1ST")

        val result = inputJson.validate[AddressLookupResult].asEither.value
        result mustBe expectedModel
      }

      "must fail to read when postcode is missing" in {
        val inputJson = Json.obj("records" -> Json.arr())

        val result = inputJson.validate[AddressLookupResult].asEither
        result.isLeft mustBe true
        result.left.value.head._2.head.message mustEqual "Invalid AddressLookupResult"
      }

      "must fail to read when postcode is not a string" in {
        val inputJson = Json.obj("postcode" -> 12345)

        val result = inputJson.validate[AddressLookupResult].asEither
        result.isLeft mustBe true
      }
    }

    "writes" - {

      "must write AddressRecords correctly" in {
        val inputModel   = AddressRecords("TE1 1ST", Seq(buildAddressRecord()))
        val expectedJson = Json.obj("postcode" -> "TE1 1ST", "records" -> Json.arr(buildAddressRecordJson()))

        val result = Json.toJson(inputModel: AddressLookupResult)
        result mustBe expectedJson
      }

      "must write NoAddressFound correctly" in {
        val inputModel   = NoAddressFound("TE1 1ST")
        val expectedJson = Json.obj("postcode" -> "TE1 1ST")

        val result = Json.toJson(inputModel: AddressLookupResult)
        result mustBe expectedJson
      }

      "must write AddressRecords with multiple records" in {
        val inputModel   = AddressRecords(
          "TE1 1ST",
          Seq(buildAddressRecord("test-id-1"), buildAddressRecord("test-id-2", None))
        )
        val expectedJson = Json.obj(
          "postcode" -> "TE1 1ST",
          "records"  -> Json.arr(
            buildAddressRecordJson("test-id-1"),
            buildAddressRecordJson("test-id-2", includePoBox = false)
          )
        )

        val result = Json.toJson(inputModel: AddressLookupResult)
        result mustBe expectedJson
      }
    }

    "round trip" - {

      "must round trip AddressRecords" in {
        val originalModel = AddressRecords("TE1 1ST", Seq(buildAddressRecord()))

        val json   = Json.toJson(originalModel: AddressLookupResult)
        val result = json.validate[AddressLookupResult].asEither.value
        result mustBe originalModel
      }

      "must round trip NoAddressFound" in {
        val originalModel = NoAddressFound("TE1 1ST")

        val json   = Json.toJson(originalModel: AddressLookupResult)
        val result = json.validate[AddressLookupResult].asEither.value
        result mustBe originalModel
      }
    }
  }

  "AddressRecords" - {

    "apply method" - {

      "must sort records by id" in {
        val unsortedInput = Seq(
          buildAddressRecord("id-3"),
          buildAddressRecord("id-1"),
          buildAddressRecord("id-2")
        )
        val expectedModel = AddressRecords(
          "TE1 1ST",
          Seq(
            buildAddressRecord("id-1"),
            buildAddressRecord("id-2"),
            buildAddressRecord("id-3")
          )
        )

        val result = AddressRecords("TE1 1ST", unsortedInput)
        result mustBe expectedModel
      }
    }
  }
}

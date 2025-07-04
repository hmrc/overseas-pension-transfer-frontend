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

package connectors

import base.BaseISpec
import models.address._
import play.api.http.Status.BAD_REQUEST
import play.api.test.Injecting
import stubs.AddressLookupStub

import scala.concurrent.ExecutionContext.Implicits.global

class AddressLookupConnectorISpec extends BaseISpec with Injecting {

  val connector: AddressLookupConnector = inject[AddressLookupConnector]

  val expectedRecords: Seq[AddressRecord] = Seq(
    AddressRecord(
      id      = "GB200000698110",
      address = RawAddress(
        lines    = List("2 Test Close"),
        town     = "Test Town",
        postcode = "BB00 1BB",
        country  = Country("GB", "United Kingdom")
      ),
      poBox   = Some("1234")
    ),
    AddressRecord(
      id      = "GB200000708497",
      address = RawAddress(
        lines    = List("4 Test Close"),
        town     = "Test Town",
        postcode = "BB00 1BB",
        country  = Country("GB", "United Kingdom")
      ),
      poBox   = Some("5678")
    )
  )

  "AddressLookupConnector.lookup" when {
    "search for address by postcode" must {
      "return an AddressLookupSuccessResponse with address records" in {
        AddressLookupStub.postPostcodeSuccessResponse()
        await(connector.lookup("BB001BB")) shouldBe
          AddressLookupSuccessResponse("BB001BB", expectedRecords)
      }

      "return an AddressLookupSuccessResponse with an empty list if no addresses found" in {
        AddressLookupStub.postPostcodeNoAddressesFoundResponse()
        await(connector.lookup("BB002BB")) shouldBe
          AddressLookupSuccessResponse("BB002BB", Seq.empty)
      }
    }

    "an exception is encountered when calling Address Lookup" must {
      "return an AddressLookupErrorResponse" in {
        AddressLookupStub.errorResponsePostPostcode("BB003BB")(BAD_REQUEST, """{"Reason":"Your submission contains one or more errors."}""")

        val result = await(connector.lookup("BB003BB"))

        result match {
          case AddressLookupErrorResponse(_) => succeed
          case _ => fail(s"Expected AddressLookupErrorResponse but got: $result")
        }
      }
    }
  }
}

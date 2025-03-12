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

package connectors

import base.BaseISpec
import models.{Address, AddressRecord, Country, RecordSet, UkAddress}
import play.api.http.Status.BAD_REQUEST
import play.api.test.Injecting
import stubs.AddressLookupStub

import scala.concurrent.ExecutionContext.Implicits.global

class AddressLookupConnectorISpec extends BaseISpec with Injecting {

  val connector: AddressLookupConnector = inject[AddressLookupConnector]

  "AddressLookupConnector.lookup" when {
    "search for address by postcode" must {
      "return a AddressLookupSuccessResponse containing a seq of addresses if address found" in {
        AddressLookupStub.postPostcodePartialSuccessResponse()
        await(connector.lookup("BB001BB")) shouldBe
        AddressLookupSuccessResponse(
          RecordSet(
            Seq(
              AddressRecord(
                id = "GB200000698110",
                address = UkAddress(
                  lines = List("2 Test Close"),
                  town = "Test Town",
                  rawPostCode = "BB00 1BB",
                  rawCountry = Country("GB", "United Kingdom")
                ),
              ),
              AddressRecord(
                id = "GB200000708497",
                address = UkAddress(
                  lines = List("4 Test Close"),
                  town = "Test Town",
                  rawPostCode = "BB00 1BB",
                  rawCountry = Country("GB", "United Kingdom")
                ),
              )
            )
          )
        )
      }

      "return an empty AddressLookupSuccessResponse if no addresses found" in {
        AddressLookupStub.postPostcodeNoAddressesFoundResponse()
        await(connector.lookup("BB002BB")) shouldBe
          AddressLookupSuccessResponse(
            RecordSet(Seq.empty)
          )
      }
    }
    "an exception is encountered when calling Address Lookup" must {
      "return an AddressLookupErrorResponse" in {
        lazy val res = {
          AddressLookupStub.errorResponsePostPostcode("BB003BB")(BAD_REQUEST, """{"Reason":"Your submission contains one or more errors."}""")

          await(connector.lookup("BB003BB"))
        }
        try {
          res
        }
        catch {
          case e: Exception => res shouldBe AddressLookupErrorResponse(e)
        }
      }
    }
  }
}
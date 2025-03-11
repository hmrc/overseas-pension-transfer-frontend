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

package stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.test.Helpers._

object AddressLookupStub {

  def stubPost(url: String, requestBody: Option[String] = None, status: Integer, responseBody: String): StubMapping =
    stubFor(post(urlMatching(url))
      .withRequestBody(equalToJson(requestBody.getOrElse("")))
      .willReturn(
        aResponse()
          .withStatus(status)
          .withBody(responseBody)
      )
    )

  def responsePostPostcode(postcode: String)(status: Int, body: String): Unit = {
    stubPost("/lookup", Some(s"""{ "postcode": "$postcode" }"""), status, body)
  }

  def errorResponsePostPostcode(postcode: String)(status: Int, body: String): Unit = {
    stubPost("/lookup", Some(s"""{ "postcode": "$postcode" }"""), status, body)
  }

  val noAddressesFoundResponseJson: String =
    s"""
       |[]
       |""".stripMargin

  val partialSuccessResponse: String =
    s"""
       |[
       |    {
       |        "id": "GB200000698110",
       |        "uprn": 200000698110,
       |        "address": {
       |            "lines": [
       |                "2 Test Close"
       |            ],
       |            "town": "Test Town",
       |            "postcode": "BB00 1BB",
       |            "subdivision": {
       |                "code": "GB-ENG",
       |                "name": "England"
       |            },
       |            "country": {
       |                "code": "GB",
       |                "name": "United Kingdom"
       |            }
       |        },
       |        "localCustodian": {
       |            "code": 1760,
       |            "name": "Test Valley"
       |        },
       |        "language": "en"
       |    },
       |    {
       |        "id": "GB200000708497",
       |        "uprn": 200000708497,
       |        "address": {
       |            "lines": [
       |                "4 Test Close"
       |            ],
       |            "town": "Test Town",
       |            "postcode": "BB00 1BB",
       |            "subdivision": {
       |                "code": "GB-ENG",
       |                "name": "England"
       |            },
       |            "country": {
       |                "code": "GB",
       |                "name": "United Kingdom"
       |            }
       |        },
       |        "localCustodian": {
       |            "code": 1760,
       |            "name": "Test Valley"
       |        },
       |        "language": "en"
       |    }
       |]
       |""".stripMargin

  val fullSuccessResponseJson: String =
    s"""
       |[
       |    {
       |        "id": "GB200000698110",
       |        "uprn": 200000698110,
       |        "address": {
       |            "lines": [
       |                "2 Test Close"
       |            ],
       |            "town": "Test Town",
       |            "postcode": "BB00 0BB",
       |            "subdivision": {
       |                "code": "GB-ENG",
       |                "name": "England"
       |            },
       |            "country": {
       |                "code": "GB",
       |                "name": "United Kingdom"
       |            }
       |        },
       |        "localCustodian": {
       |            "code": 1760,
       |            "name": "Test Valley"
       |        },
       |        "language": "en"
       |    },
       |    {
       |        "id": "GB200000708497",
       |        "uprn": 200000708497,
       |        "address": {
       |            "lines": [
       |                "4 Test Close"
       |            ],
       |            "town": "Test Town",
       |            "postcode": "BB00 0BB",
       |            "subdivision": {
       |                "code": "GB-ENG",
       |                "name": "England"
       |            },
       |            "country": {
       |                "code": "GB",
       |                "name": "United Kingdom"
       |            }
       |        },
       |        "localCustodian": {
       |            "code": 1760,
       |            "name": "Test Valley"
       |        },
       |        "language": "en"
       |    },
       |    {
       |        "id": "GB200000704710",
       |        "uprn": 200000704710,
       |        "address": {
       |            "lines": [
       |                "6 Test Close"
       |            ],
       |            "town": "Test Town",
       |            "postcode": "BB00 0BB",
       |            "subdivision": {
       |                "code": "GB-ENG",
       |                "name": "England"
       |            },
       |            "country": {
       |                "code": "GB",
       |                "name": "United Kingdom"
       |            }
       |        },
       |        "localCustodian": {
       |            "code": 1760,
       |            "name": "Test Valley"
       |        },
       |        "language": "en"
       |    },
       |    {
       |        "id": "GB200000700558",
       |        "uprn": 200000700558,
       |        "address": {
       |            "lines": [
       |                "8 Test Close"
       |            ],
       |            "town": "Test Town",
       |            "postcode": "BB00 0BB",
       |            "subdivision": {
       |                "code": "GB-ENG",
       |                "name": "England"
       |            },
       |            "country": {
       |                "code": "GB",
       |                "name": "United Kingdom"
       |            }
       |        },
       |        "localCustodian": {
       |            "code": 1760,
       |            "name": "Test Valley"
       |        },
       |        "language": "en"
       |    },
       |    {
       |        "id": "GB200010012154",
       |        "uprn": 200010012154,
       |        "address": {
       |            "lines": [
       |                "Test Lodge",
       |                "Test Close"
       |            ],
       |            "town": "Test Town",
       |            "postcode": "BB00 0BB",
       |            "subdivision": {
       |                "code": "GB-ENG",
       |                "name": "England"
       |            },
       |            "country": {
       |                "code": "GB",
       |                "name": "United Kingdom"
       |            }
       |        },
       |        "localCustodian": {
       |            "code": 1760,
       |            "name": "Test Valley"
       |        },
       |        "language": "en"
       |    },
       |    {
       |        "id": "GB200000706253",
       |        "uprn": 200000706253,
       |        "address": {
       |            "lines": [
       |                "Test House",
       |                "Test Close"
       |            ],
       |            "town": "Test Town",
       |            "postcode": "BB00 0BB",
       |            "subdivision": {
       |                "code": "GB-ENG",
       |                "name": "England"
       |            },
       |            "country": {
       |                "code": "GB",
       |                "name": "United Kingdom"
       |            }
       |        },
       |        "localCustodian": {
       |            "code": 1760,
       |            "name": "Test Valley"
       |        },
       |        "language": "en"
       |    }
       |]
       |""".stripMargin

  def postPostcodeFullSuccessResponse(): Unit = responsePostPostcode("BB000BB")(OK, fullSuccessResponseJson)

  def postPostcodePartialSuccessResponse(): Unit = responsePostPostcode("BB001BB")(OK, partialSuccessResponse)

  def postPostcodeNoAddressesFoundResponse(): Unit = responsePostPostcode("BB002BB")(OK, noAddressesFoundResponseJson)

}

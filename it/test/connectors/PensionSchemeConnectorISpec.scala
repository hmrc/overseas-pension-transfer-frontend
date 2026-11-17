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

import base.ISpecBase
import models.authentication.*
import models.responses.{PensionSchemeErrorResponse, PensionSchemeNotAssociated}
import models.PensionSchemeResponse
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import play.api.test.Helpers.*
import stubs.PensionSchemeStub

class PensionSchemeConnectorISpec extends ISpecBase {

  val connector: PensionSchemeConnector = app.injector.instanceOf[PensionSchemeConnector]

  "PensionSchemeConnector.checkAssociation" when {

    "checking association status" must {

      "return true for PSA when downstream says associated and headers are present" in {
        PensionSchemeStub.checkAssociationPsaTrue(srn.value)
        await(connector.checkAssociation(srn.value, psaUser)) mustBe true
      }

      "return false for PSP when downstream says not associated and headers are present" in {
        PensionSchemeStub.checkAssociationPspFalse(srn.value)
        await(connector.checkAssociation(srn.value, pspUser)) mustBe false
      }
    }
  }

  "PensionSchemeConnector.getSchemeDetails" when {

    "fetching scheme details" must {

      "return Right(PensionSchemeResponse) for PSA route" in {
        PensionSchemeStub.stubGetSchemeDetailsForPsaSuccess(srn.value, pstr.value, schemeName)

        await(connector.getSchemeDetails(srn.value, psaUser)) mustBe
          Right(PensionSchemeResponse(pstr, schemeName))
      }

      "return Right(PensionSchemeResponse) for PSP route" in {
        PensionSchemeStub.stubGetSchemeDetailsForPspSuccess(srn.value, pstr.value, schemeName)

        await(connector.getSchemeDetails(srn.value, pspUser)) mustBe
          Right(PensionSchemeResponse(pstr, schemeName))
      }

      "return Left(PensionSchemeNotAssociated) on 404" in {
        PensionSchemeStub.stubGetSchemeDetailsNotAssociatedForPsa(srn.value)

        val result = await(connector.getSchemeDetails(srn.value, psaUser))
        result match {
          case Left(_: PensionSchemeNotAssociated) => succeed
          case _                                   => fail(s"Expected PensionSchemeNotAssociated but got: $result")
        }
      }

      "return Left(PensionSchemeErrorResponse) on 200 with invalid JSON" in {
        PensionSchemeStub.responseGetSchemeDetailsForPsa(srn.value)(
          status = OK,
          body = s"""{
               |  "srn": "${srn.value}",
               |  "pstr": "${pstr.value}"
               |}""".stripMargin
        )

        val result = await(connector.getSchemeDetails(srn.value, psaUser))
        result match {
          case Left(PensionSchemeErrorResponse(msg, maybeDetails)) =>
            msg.toLowerCase          must include("unable to parse json")
            maybeDetails.isDefined mustBe true
          case _                                                   =>
            fail(s"Expected PensionSchemeErrorResponse (invalid JSON) but got: $result")
        }
      }

      "return Left(PensionSchemeErrorResponse) on 500 with JSON error body" in {
        PensionSchemeStub.stubGetSchemeDetailsErrorForPsp(
          srn = srn.value,
          status = INTERNAL_SERVER_ERROR,
          body = """{"error":"downstream boom"}"""
        )

        val result = await(connector.getSchemeDetails(srn.value, pspUser))
        result match {
          case Left(PensionSchemeErrorResponse(msg, _)) =>
            msg must include("downstream boom")
          case _                                        =>
            fail(s"Expected PensionSchemeErrorResponse (500) but got: $result")
        }
      }

      "return Left(PensionSchemeErrorResponse) on 500 with JSON parsing errors, when the error cannot parse" in {
        PensionSchemeStub.stubGetSchemeDetailsErrorForPsp(
          srn = srn.value,
          status = INTERNAL_SERVER_ERROR,
          body = Json.stringify(
            Json.obj(
              "invalidField" -> 17
            )
          )
        )

        val result = await(connector.getSchemeDetails(srn.value, pspUser))
        result match {
          case Left(PensionSchemeErrorResponse(msg, _)) =>
            msg must include("500 Unknown (correlationId=-)")
          case _                                        =>
            fail(s"Expected PensionSchemeErrorResponse (500) but got: $result")
        }
      }

      "return Left(PensionSchemeErrorResponse) when a network fault occurs" in {
        PensionSchemeStub.faultGetSchemeDetailsForPsa(srn.value)

        val result = await(connector.getSchemeDetails(srn.value, psaUser))
        result match {
          case Left(PensionSchemeErrorResponse(msg, _)) =>
            msg.nonEmpty mustBe true
          case _                                        =>
            fail(s"Expected PensionSchemeErrorResponse (fault) but got: $result")
        }
      }
    }
  }

  "PensionSchemeConnector.getIsAuthorisingPsa" when {

    "fetching the authorising PSA ID" must {

      "return Right(PsaId) when downstream returns valid JSON with authorisingPSAID" in {
        PensionSchemeStub.stubGetAuthorisingPsaSuccess(srn.value, "A2100005")

        val result = await(connector.getAuthorisingPsa(srn.value))

        result mustBe Right(PsaId("A2100005"))
      }

      "return Left(PensionSchemeNotAssociated) on 404" in {
        PensionSchemeStub.stubGetAuthorisingPsaNotAssociated(srn.value)

        val result = await(connector.getAuthorisingPsa(srn.value))

        result match {
          case Left(_: PensionSchemeNotAssociated) => succeed
          case _                                   => fail(s"Expected PensionSchemeNotAssociated but got: $result")
        }
      }

      "return Left(PensionSchemeErrorResponse) on 200 with invalid JSON (missing authorisingPSAID)" in {
        PensionSchemeStub.responseGetAuthorisingPsa(
          srn = srn.value
        )(
          status = OK,
          body = s"""{
               |  "pspDetails": {
               |    "id": "21000005",
               |    "individual": {
               |      "firstName": "PSP Individual",
               |      "lastName": "UK"
               |    }
               |  }
               |}""".stripMargin
        )

        val result = await(connector.getAuthorisingPsa(srn.value))

        result match {
          case Left(PensionSchemeErrorResponse(msg, maybeDetails)) =>
            msg.toLowerCase          must include("unable to parse json")
            maybeDetails.isDefined mustBe true
          case _                                                   =>
            fail(s"Expected PensionSchemeErrorResponse (invalid JSON) but got: $result")
        }
      }

      "return Left(PensionSchemeErrorResponse) on 500 with JSON error body" in {
        PensionSchemeStub.stubGetAuthorisingPsaError(
          srn = srn.value
        )(
          status = INTERNAL_SERVER_ERROR,
          body = """{"error":"downstream boom"}"""
        )

        val result = await(connector.getAuthorisingPsa(srn.value))

        result match {
          case Left(PensionSchemeErrorResponse(msg, _)) =>
            msg must include("downstream boom")
          case _                                        =>
            fail(s"Expected PensionSchemeErrorResponse (500) but got: $result")
        }
      }

      "return Left(PensionSchemeErrorResponse) on 500 with JSON parsing errors, when the error cannot parse" in {
        PensionSchemeStub.stubGetAuthorisingPsaError(
          srn = srn.value
        )(
          status = INTERNAL_SERVER_ERROR,
          body = Json.stringify(
            Json.obj(
              "invalidField" -> 42
            )
          )
        )

        val result = await(connector.getAuthorisingPsa(srn.value))

        result match {
          case Left(PensionSchemeErrorResponse(msg, _)) =>
            msg must include("500 Unknown (correlationId=-)")
          case _                                        =>
            fail(s"Expected PensionSchemeErrorResponse (500) but got: $result")
        }
      }

      "return Left(PensionSchemeErrorResponse) when a network fault occurs" in {
        PensionSchemeStub.faultGetAuthorisingPsa(srn.value)

        val result = await(connector.getAuthorisingPsa(srn.value))

        result match {
          case Left(PensionSchemeErrorResponse(msg, _)) =>
            msg.nonEmpty mustBe true
          case _                                        =>
            fail(s"Expected PensionSchemeErrorResponse (fault) but got: $result")
        }
      }
    }
  }
}

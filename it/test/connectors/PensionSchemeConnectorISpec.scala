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
import models.authentication._
import models.responses.{PensionSchemeErrorResponse, PensionSchemeNotAssociated}
import models.{PensionSchemeDetails, PstrNumber, SrnNumber}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.test.Injecting
import stubs.PensionSchemeStub

class PensionSchemeConnectorISpec extends BaseISpec with Injecting {

  val connector: PensionSchemeConnector = inject[PensionSchemeConnector]

  val srn: String      = "S2400000040"
  val pstr: String     = "24000040IN"
  val schemeNm: String = "Open Scheme Overview API Test"

  val psaUser: AuthenticatedUser = PsaUser(PsaId("A2100005"), "ext-psa", None)
  val pspUser: AuthenticatedUser = PspUser(PspId("21000005"), "ext-psp", None)

  "PensionSchemeConnector.checkAssociation" when {
    "checking association status" must {
      "return true for PSA when downstream says associated and headers are present" in {
        PensionSchemeStub.checkAssociationPsaTrue(srn)
        await(connector.checkAssociation(srn, psaUser)) shouldBe true
      }

      "return false for PSP when downstream says not associated and headers are present" in {
        PensionSchemeStub.checkAssociationPspFalse(srn)
        await(connector.checkAssociation(srn, pspUser)) shouldBe false
      }
    }
  }

  "PensionSchemeConnector.getSchemeDetails" when {
    "fetching scheme details" must {
      "return Right(PensionSchemeDetails) for PSA route" in {
        PensionSchemeStub.getSchemeDetailsForPsaSuccess(srn, pstr, schemeNm)

        await(connector.getSchemeDetails(srn, psaUser)) shouldBe
          Right(PensionSchemeDetails(SrnNumber(srn), PstrNumber(pstr), schemeNm))
      }

      "return Right(PensionSchemeDetails) for PSP route" in {
        PensionSchemeStub.getSchemeDetailsForPspSuccess(srn, pstr, schemeNm)

        await(connector.getSchemeDetails(srn, pspUser)) shouldBe
          Right(PensionSchemeDetails(SrnNumber(srn), PstrNumber(pstr), schemeNm))
      }

      "return Left(PensionSchemeNotAssociated) on 404" in {
        PensionSchemeStub.getSchemeDetailsNotAssociatedForPsa(srn)

        val result = await(connector.getSchemeDetails(srn, psaUser))
        result match {
          case Left(_: PensionSchemeNotAssociated) => succeed
          case _                                   => fail(s"Expected PensionSchemeNotAssociated but got: $result")
        }
      }

      "return Left(PensionSchemeErrorResponse) on 200 with invalid JSON" in {
        PensionSchemeStub.responseGetSchemeDetailsForPsa(srn)(
          status = OK,
          body   =
            s"""{
               |  "srn": "$srn",
               |  "pstr": "$pstr"
               |}""".stripMargin
        )

        val result = await(connector.getSchemeDetails(srn, psaUser))
        result match {
          case Left(PensionSchemeErrorResponse(msg, maybeDetails)) =>
            msg.toLowerCase should include ("unable to parse json")
            maybeDetails.isDefined shouldBe true
          case _ =>
            fail(s"Expected PensionSchemeErrorResponse (invalid JSON) but got: $result")
        }
      }

      "return Left(PensionSchemeErrorResponse) on 500 with JSON error body" in {
        PensionSchemeStub.getSchemeDetailsErrorForPsp(
          srn   = srn,
          status= INTERNAL_SERVER_ERROR,
          body  = """{"error":"downstream boom"}"""
        )

        val result = await(connector.getSchemeDetails(srn, pspUser))
        result match {
          case Left(PensionSchemeErrorResponse(msg, _)) =>
            msg should include ("downstream boom")
          case _ =>
            fail(s"Expected PensionSchemeErrorResponse (500) but got: $result")
        }
      }

      "return Left(PensionSchemeErrorResponse) when a network fault occurs" in {
        PensionSchemeStub.faultGetSchemeDetailsForPsa(srn)

        val result = await(connector.getSchemeDetails(srn, psaUser))
        result match {
          case Left(PensionSchemeErrorResponse(msg, _)) =>
            msg.nonEmpty shouldBe true
          case _ =>
            fail(s"Expected PensionSchemeErrorResponse (fault) but got: $result")
        }
      }
    }
  }
}

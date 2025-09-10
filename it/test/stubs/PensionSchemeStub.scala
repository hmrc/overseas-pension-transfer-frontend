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

package stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.test.Helpers._

object PensionSchemeStub {

  private def stubGet(
                       urlRegex: String,
                       status: Int,
                       responseBody: String,
                       requiredHeaders: Seq[(String, String)] = Seq.empty,
                       headerRegexes: Seq[(String, String)] = Seq.empty,
                       absentHeaders: Seq[String] = Seq.empty
                     ): StubMapping = {

    val builder = get(urlPathMatching(urlRegex)).atPriority(1)

    val withExactHeaders   = requiredHeaders.foldLeft(builder) { case (b, (k, v)) => b.withHeader(k, equalTo(v)) }
    val withRegexHeaders   = headerRegexes.foldLeft(withExactHeaders) { case (b, (k, r)) => b.withHeader(k, matching(r)) }
    val withAbsentHeaders  = absentHeaders.foldLeft(withRegexHeaders) { case (b, k) => b.withHeader(k, absent()) }

    stubFor(
      withAbsentHeaders.willReturn(
        aResponse()
          .withStatus(status)
          .withBody(responseBody)
          .withHeader("Content-Type", "application/json")
      )
    )
  }

  private val AssocPathRegex        = ".*/pensions-scheme/is-psa-associated"
  private def PsaDetailsPath(srn: String) = s".*/pensions-scheme/scheme/$srn"
  private def PspDetailsPath(srn: String) = s".*/pensions-scheme/psp-scheme/$srn"

  def responseCheckAssociationPsa(srn: String)(status: Int, body: String): Unit =
    stubGet(
      urlRegex        = AssocPathRegex,
      status          = status,
      responseBody    = body,
      requiredHeaders = Seq("schemeReferenceNumber" -> srn),
      headerRegexes   = Seq("psaId" -> "^A\\d+$"),
      absentHeaders   = Seq("pspId")
    )

  def responseCheckAssociationPsp(srn: String)(status: Int, body: String): Unit =
    stubGet(
      urlRegex        = AssocPathRegex,
      status          = status,
      responseBody    = body,
      requiredHeaders = Seq("schemeReferenceNumber" -> srn),
      headerRegexes   = Seq("pspId" -> "^\\d+$"),
      absentHeaders   = Seq("psaId")
    )

  def checkAssociationPsaTrue(srn: String): Unit  = responseCheckAssociationPsa(srn)(OK,    "true")
  def checkAssociationPsaFalse(srn: String): Unit = responseCheckAssociationPsa(srn)(OK,    "false")
  def checkAssociationPspTrue(srn: String): Unit  = responseCheckAssociationPsp(srn)(OK,    "true")
  def checkAssociationPspFalse(srn: String): Unit = responseCheckAssociationPsp(srn)(OK,    "false")

  def responseGetSchemeDetailsForPsa(srn: String)(status: Int, body: String): Unit =
    stubGet(
      urlRegex        = PsaDetailsPath(srn),
      status          = status,
      responseBody    = body,
      requiredHeaders = Seq(
        "schemeIdType" -> "srn",
        "idNumber"     -> srn
      )
    )

  def responseGetSchemeDetailsForPsp(srn: String)(status: Int, body: String): Unit =
    stubGet(
      urlRegex        = PspDetailsPath(srn),
      status          = status,
      responseBody    = body,
      requiredHeaders = Seq("srn" -> srn)
    )

  def getSchemeDetailsForPsaSuccess(srn: String, pstr: String, schemeName: String): Unit =
    responseGetSchemeDetailsForPsa(srn)(OK, schemeDetailsJson(srn, pstr, schemeName))

  def getSchemeDetailsForPspSuccess(srn: String, pstr: String, schemeName: String): Unit =
    responseGetSchemeDetailsForPsp(srn)(OK, schemeDetailsJson(srn, pstr, schemeName))

  def getSchemeDetailsNotAssociatedForPsa(srn: String): Unit =
    responseGetSchemeDetailsForPsa(srn)(NOT_FOUND, """{"error":"not associated"}""")

  def getSchemeDetailsErrorForPsp(srn: String, status: Int, body: String): Unit =
    responseGetSchemeDetailsForPsp(srn)(status, body)

  def faultGetSchemeDetailsForPsa(srn: String): StubMapping =
    stubFor(
      get(urlPathMatching(PsaDetailsPath(srn)))
        .withHeader("schemeIdType", equalTo("srn"))
        .withHeader("idNumber", equalTo(srn))
        .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER))
    )

  def schemeDetailsJson(srn: String, pstr: String, schemeName: String): String =
    s"""
       |{
       |  "srn": "$srn",
       |  "pstr": "$pstr",
       |  "schemeName": "$schemeName"
       |}
       |""".stripMargin
}

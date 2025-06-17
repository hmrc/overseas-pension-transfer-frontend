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

package utils

import config.FrontendAppConfig
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}

class AuthSupportSpec extends AnyFreeSpec with Matchers with MockitoSugar with AuthSupport {

  private val psaServiceName = "HMRC-PSA-ORG"
  private val pspServiceName = "HMRC-PSP-ORG"
  private val psaIdKey       = "PSAID"
  private val pspIdKey       = "PSPID"

  private val mockAppConfig = mock[FrontendAppConfig]
  private val psaConfig     = new mockAppConfig.EnrolmentConfig(psaServiceName, psaIdKey)
  private val pspConfig     = new mockAppConfig.EnrolmentConfig(pspServiceName, pspIdKey)

  when(mockAppConfig.psaEnrolment).thenReturn(psaConfig)
  when(mockAppConfig.pspEnrolment).thenReturn(pspConfig)

  "buildPredicate" - {
    "should construct predicate with GovernmentGateway and PSA/PSP enrolments" in {
      val predicate = buildPredicate(mockAppConfig)
      predicate.toString must include("GovernmentGateway")
      predicate.toString must include(psaServiceName)
      predicate.toString must include(pspServiceName)
    }
  }

  "extractPsaPspId" - {

    "should return PSA ID when PSA enrolment is present" in {
      val psaId      = "A1234567"
      val enrolment  = Enrolment(psaServiceName, Seq(EnrolmentIdentifier(psaIdKey, psaId)), "Activated")
      val enrolments = Enrolments(Set(enrolment))

      extractPsaPspId(enrolments, mockAppConfig) mustBe psaId
    }

    "should return PSP ID when PSP enrolment is present" in {
      val pspId      = "X9999999"
      val enrolment  = Enrolment(pspServiceName, Seq(EnrolmentIdentifier(pspIdKey, pspId)), "Activated")
      val enrolments = Enrolments(Set(enrolment))

      extractPsaPspId(enrolments, mockAppConfig) mustBe pspId
    }

    "should throw if no matching enrolment found" in {
      val enrolment  = Enrolment("OTHER-KEY", Seq(EnrolmentIdentifier("OTHER-ID", "VALUE")), "Activated")
      val enrolments = Enrolments(Set(enrolment))

      val ex = intercept[IllegalStateException] {
        extractPsaPspId(enrolments, mockAppConfig)
      }
      ex.getMessage must include("Unable to retrieve matching PSA or PSP enrolment")
    }

    "should throw if matching enrolment is missing identifier" in {
      val enrolment  = Enrolment(psaServiceName, Seq.empty, "Activated")
      val enrolments = Enrolments(Set(enrolment))

      val ex = intercept[IllegalStateException] {
        extractPsaPspId(enrolments, mockAppConfig)
      }
      ex.getMessage must include(s"Unable to retrieve identifier from enrolment $psaServiceName")
    }
  }

  "getOrElseFailWithUnauthorised" - {
    "should return value if Some(value)" in {
      getOrElseFailWithUnauthorised(Some("abc"), "fail") mustBe "abc"
    }

    "should throw if None" in {
      val ex = intercept[IllegalStateException] {
        getOrElseFailWithUnauthorised(None, "missing value")
      }
      ex.getMessage must include("missing value")
    }
  }
}

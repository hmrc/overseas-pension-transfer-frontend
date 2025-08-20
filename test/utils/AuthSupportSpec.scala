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
import models.authentication.{PsaId, PsaUser, PspId, PspUser}
import org.mockito.Mockito._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}

class AuthSupportSpec extends AnyFreeSpec with Matchers with MockitoSugar with AuthSupport {

  private val psaServiceName = "HMRC-PSA-ORG"
  private val pspServiceName = "HMRC-PSP-ORG"
  private val psaIdKey       = "PSAID"
  private val pspIdKey       = "PSPID"
  private val internalId     = "internal-123"

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

  "extractUser" - {
    "should return PsaUser when PSA enrolment is present" in {
      val psaId      = PsaId("A123456")
      val enrolment  = Enrolment(psaServiceName, Seq(EnrolmentIdentifier(psaIdKey, psaId.value)), "Activated")
      val enrolments = Enrolments(Set(enrolment))

      val result = extractUser(enrolments, mockAppConfig, internalId)
      result mustBe PsaUser(psaId, internalId)
    }

    "should return PspUser when PSP enrolment is present" in {
      val pspId      = PspId("X9999999")
      val enrolment  = Enrolment(pspServiceName, Seq(EnrolmentIdentifier(pspIdKey, pspId.value)), "Activated")
      val enrolments = Enrolments(Set(enrolment))

      val result = extractUser(enrolments, mockAppConfig, internalId)
      result mustBe PspUser(pspId, internalId)
    }

    "should throw if no matching enrolment found" in {
      val enrolment  = Enrolment("OTHER-KEY", Seq(EnrolmentIdentifier("OTHER-ID", "VALUE")), "Activated")
      val enrolments = Enrolments(Set(enrolment))

      val ex = intercept[RuntimeException] {
        extractUser(enrolments, mockAppConfig, internalId)
      }
      ex.getMessage must include("Unable to retrieve matching PSA or PSP enrolment")
    }

    "should throw if matching enrolment is missing identifier" in {
      val enrolment  = Enrolment(psaServiceName, Seq.empty, "Activated")
      val enrolments = Enrolments(Set(enrolment))

      val ex = intercept[RuntimeException] {
        extractUser(enrolments, mockAppConfig, internalId)
      }
      ex.getMessage must include(s"Missing identifier for $psaServiceName")
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

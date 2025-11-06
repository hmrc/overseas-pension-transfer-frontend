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

package controllers.actions

import base.SpecBase
import connectors.PensionSchemeConnector
import models.{DashboardData, PensionSchemeDetails, PstrNumber, SrnNumber}
import models.authentication.{PsaId, PsaUser}
import models.requests.{IdentifierRequest, SchemeRequest}
import models.responses.PensionSchemeErrorResponse
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status.SEE_OTHER
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import repositories.DashboardSessionRepository
import uk.gov.hmrc.auth.core.AffinityGroup.Individual

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SchemeDataActionSpec extends AnyFreeSpec with SpecBase {

  private val mockSessionRepository                              = mock[DashboardSessionRepository]
  private val mockPensionSchemeConnector: PensionSchemeConnector = mock[PensionSchemeConnector]

  class Harness(pensionSchemeConnector: PensionSchemeConnector, sessionRepository: DashboardSessionRepository)
      extends SchemeDataActionImpl(pensionSchemeConnector, sessionRepository) {
    def callRefine[A](request: IdentifierRequest[A]): Future[Either[Result, SchemeRequest[A]]] = refine(request)
  }

  "refine" - {
    "return Right of SchemeData request" - {
      "when authenticatedUser has NO existing pensionSchemeDetails and checkAssociation returns true" in {
        val dataJson = Json.obj("pensionSchemeDetails" -> Json.obj("srnNumber" -> "S1234567", "pstrNumber" -> "12345678AB", "schemeName" -> "Scheme Name"))

        when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(DashboardData("id", dataJson)))
        when(mockPensionSchemeConnector.checkAssociation(any(), any())(any())) thenReturn Future.successful(true)

        val identifierRequest = IdentifierRequest(
          FakeRequest(),
          PsaUser(
            PsaId("psaId"),
            "internalId",
            affinityGroup = Individual
          )
        )

        val refine = new Harness(mockPensionSchemeConnector, mockSessionRepository).callRefine(identifierRequest).futureValue

        refine.map {
          request =>
            request.authenticatedUser mustBe
              PsaUser(
                PsaId("psaId"),
                "internalId",
                affinityGroup = Individual
              )

            request.schemeDetails mustBe
              PensionSchemeDetails(
                SrnNumber("S1234567"),
                PstrNumber("12345678AB"),
                "Scheme Name"
              )
        }
      }

      "when authenticatedUser has existing pensionSchemeDetails" in {

        val identifierRequest = IdentifierRequest(
          FakeRequest(),
          PsaUser(
            PsaId("psaId"),
            "internalId",
            affinityGroup = Individual
          )
        )

        val refine = new Harness(mockPensionSchemeConnector, mockSessionRepository).callRefine(identifierRequest).futureValue

        refine.map {
          request =>
            request.authenticatedUser mustBe
              identifierRequest.authenticatedUser
        }
      }

      "when dashboard data returns none but On Ramp request provides Srn and completes isAssociated and GetSchemeDetails" in {
        val schemeDetails = PensionSchemeDetails(
          SrnNumber("S1234567"),
          PstrNumber("12345678AB"),
          "Scheme Name"
        )

        when(mockSessionRepository.get(any())) thenReturn Future.successful(None)
        when(mockPensionSchemeConnector.checkAssociation(any(), any())(any())) thenReturn Future.successful(true)
        when(mockPensionSchemeConnector.getSchemeDetails(any(), any())(any())) thenReturn Future.successful(Right(schemeDetails))

        val identifierRequest = IdentifierRequest(
          FakeRequest("GET", "/start?srn=S1234567"),
          PsaUser(
            PsaId("psaId"),
            "internalId",
            affinityGroup = Individual
          )
        )

        val refine = new Harness(mockPensionSchemeConnector, mockSessionRepository).callRefine(identifierRequest).futureValue

        refine.map {
          request =>
            request.authenticatedUser mustBe
              PsaUser(
                PsaId("psaId"),
                "internalId",
                affinityGroup = Individual
              )

            request.schemeDetails mustBe
              PensionSchemeDetails(
                SrnNumber("S1234567"),
                PstrNumber("12345678AB"),
                "Scheme Name"
              )
        }
      }

    }

    "return Left Redirect to Unauthorised when checkAssociation returns false" in {
      val dataJson = Json.obj("pensionSchemeDetails" -> Json.obj("srnNumber" -> "S1234567", "pstrNumber" -> "12345678AB", "schemeName" -> "Scheme Name"))

      when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(DashboardData("id", dataJson)))
      when(mockPensionSchemeConnector.checkAssociation(any(), any())(any())) thenReturn Future.successful(false)

      val identifierRequest = IdentifierRequest(
        FakeRequest(),
        PsaUser(
          PsaId("psaId"),
          "internalId",
          affinityGroup = Individual
        )
      )

      val refine = new Harness(mockPensionSchemeConnector, mockSessionRepository).callRefine(identifierRequest).futureValue

      refine.left.map {
        result =>
          result.header.status mustBe SEE_OTHER
          result.header.headers.get("Location") mustBe Some(controllers.auth.routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "Return Left and redirect to Journey Recovery" - {
      "when there is no srn found" in {
        val dataJson = Json.obj("pensionSchemeDetails" -> Json.obj())

        when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(DashboardData("id", dataJson)))

        val identifierRequest = IdentifierRequest(
          FakeRequest(),
          PsaUser(
            PsaId("psaId"),
            "internalId",
            affinityGroup = Individual
          )
        )

        val refine = new Harness(mockPensionSchemeConnector, mockSessionRepository).callRefine(identifierRequest).futureValue

        refine.left.map {
          result =>
            result.header.status mustBe SEE_OTHER
            result.header.headers.get("Location") mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
        }
      }

      "when there is no dashboard data returned and no srn is provided by on ramp request" - {
        when(mockSessionRepository.get(any())) thenReturn Future.successful(None)

        val identifierRequest =
          IdentifierRequest(
            FakeRequest(),
            PsaUser(
              PsaId("psaId"),
              "internalId",
              affinityGroup = Individual
            )
          )

        val refine = new Harness(mockPensionSchemeConnector, mockSessionRepository).callRefine(identifierRequest).futureValue

        refine.left.map {
          result =>
            result.header.status mustBe SEE_OTHER
            result.header.headers.get("Location") mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
        }
      }

      "when dashboard data returns none but On Ramp request provides Srn and isAssociated returns false" in {
        when(mockSessionRepository.get(any())) thenReturn Future.successful(None)
        when(mockPensionSchemeConnector.checkAssociation(any(), any())(any())) thenReturn Future.successful(false)

        val identifierRequest = IdentifierRequest(
          FakeRequest("GET", "/start?srn=S1234567"),
          PsaUser(
            PsaId("psaId"),
            "internalId",
            affinityGroup = Individual
          )
        )

        val refine = new Harness(mockPensionSchemeConnector, mockSessionRepository).callRefine(identifierRequest).futureValue

        refine.left.map {
          result =>
            result.header.status mustBe SEE_OTHER
            result.header.headers.get("Location") mustBe Some(controllers.auth.routes.UnauthorisedController.onPageLoad().url)
        }
      }

      "when dashboard data returns none but On Ramp request provides Srn and isAssociated returns true and getSchemeDetails returns a Left" in {
        when(mockSessionRepository.get(any())) thenReturn Future.successful(None)
        when(mockPensionSchemeConnector.checkAssociation(any(), any())(any())) thenReturn Future.successful(true)
        when(mockPensionSchemeConnector.getSchemeDetails(any(), any())(any())) thenReturn Future.successful(Left(PensionSchemeErrorResponse("Error", None)))

        val identifierRequest = IdentifierRequest(
          FakeRequest("GET", "/start?srn=S1234567"),
          PsaUser(
            PsaId("psaId"),
            "internalId",
            affinityGroup = Individual
          )
        )

        val refine = new Harness(mockPensionSchemeConnector, mockSessionRepository).callRefine(identifierRequest).futureValue

        refine.left.map {
          result =>
            result.header.status mustBe SEE_OTHER
            result.header.headers.get("Location") mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
        }
      }
    }
  }
}

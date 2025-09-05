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
import models.{DashboardData, QtNumber, SrnNumber, UserAnswers}
import models.authentication.{PsaId, PsaUser}
import models.requests.{DataRequest, DisplayRequest}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status.SEE_OTHER
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import repositories.DashboardSessionRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IsAssociatedCheckActionSpec extends AnyFreeSpec with SpecBase {

  private val mockSessionRepository                              = mock[DashboardSessionRepository]
  private val mockPensionSchemeConnector: PensionSchemeConnector = mock[PensionSchemeConnector]

  class Harness(pensionSchemeConnector: PensionSchemeConnector, sessionRepository: DashboardSessionRepository)
      extends IsAssociatedCheckActionImpl(pensionSchemeConnector, sessionRepository) {
    def callRefine[A](request: DataRequest[A]): Future[Either[Result, DisplayRequest[A]]] = refine(request)
  }

  "refine" - {
    "return Right of Display request when checkAssociation returns true" in {
      val dataJson = Json.obj("mps" -> Json.obj("srn" -> "12345"))

      when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(DashboardData("id", dataJson)))
      when(mockPensionSchemeConnector.checkAssociation(any(), any())(any())) thenReturn Future.successful(true)

      val dataRequest = DataRequest(FakeRequest(), PsaUser(PsaId("psaId"), "internalId", Some(SrnNumber("12345"))), UserAnswers("internalId"))

      val refine = new Harness(mockPensionSchemeConnector, mockSessionRepository).callRefine(dataRequest).futureValue

      refine.map {
        request =>
          request mustBe
            DisplayRequest(
              dataRequest.request,
              dataRequest.authenticatedUser,
              dataRequest.userAnswers,
              "Undefined Undefined",
              QtNumber.empty,
              "Transfer not submitted"
            )
      }
    }

    "return Left Redirect to Unauthorised when checkAssociation returns false" in {
      val dataJson = Json.obj("mps" -> Json.obj("srn" -> "12345"))

      when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(DashboardData("id", dataJson)))
      when(mockPensionSchemeConnector.checkAssociation(any(), any())(any())) thenReturn Future.successful(false)

      val dataRequest = DataRequest(FakeRequest(), PsaUser(PsaId("psaId"), "internalId", Some(SrnNumber("12345"))), UserAnswers("internalId"))

      val refine = new Harness(mockPensionSchemeConnector, mockSessionRepository).callRefine(dataRequest).futureValue

      refine.left.map {
        result =>
          result.header.status mustBe SEE_OTHER
          result.header.headers.get("Location") mustBe Some(controllers.auth.routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "Return Left and redirect to Journey Recovery" - {
      "when there is no srn found" in {
        val dataJson = Json.obj("mps" -> Json.obj())

        when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(DashboardData("id", dataJson)))

        val dataRequest = DataRequest(FakeRequest(), PsaUser(PsaId("psaId"), "internalId", Some(SrnNumber("12345"))), UserAnswers("internalId"))

        val refine = new Harness(mockPensionSchemeConnector, mockSessionRepository).callRefine(dataRequest).futureValue

        refine.left.map {
          result =>
            result.header.status mustBe SEE_OTHER
            result.header.headers.get("Location") mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
        }
      }

      "when there is no dashboard data returned" - {
        when(mockSessionRepository.get(any())) thenReturn Future.successful(None)

        val dataRequest = DataRequest(FakeRequest(), PsaUser(PsaId("psaId"), "internalId", Some(SrnNumber("12345"))), UserAnswers("internalId"))

        val refine = new Harness(mockPensionSchemeConnector, mockSessionRepository).callRefine(dataRequest).futureValue

        refine.left.map {
          result =>
            result.header.status mustBe SEE_OTHER
            result.header.headers.get("Location") mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
        }
      }
    }
  }
}

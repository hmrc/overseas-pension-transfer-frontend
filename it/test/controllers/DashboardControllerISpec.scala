/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers

import models.NormalMode
import stubs.{AuthStub, EmailStub, MinimalDetailsStub, PensionSchemeStub, TransferBackendStub}
import base.ISpecBase
import models.dtos.UserAnswersDTO
import models.responses.SubmissionResponse
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.dashboard.TransfersOverviewQuery

class DashboardControllerISpec extends ISpecBase {

  "DashboardController.onLoadPage" must {
    "return full dashboard when PSA associated to the scheme and dashboard has entries" in {
      AuthStub.authorised()
      PensionSchemeStub.checkAssociationPsaTrue(srn.value)
      dashBoardSessionRepository.set(testDashboardData).futureValue
      TransferBackendStub.stubGetAllTransfersOk(pstr.value, testNino)

      val result =
        route(app, FakeRequest(routes.DashboardController.onPageLoad(1, None)).withSession(authSession)).value

      status(result) mustBe OK
      contentAsString(result) must (include("QT564321") and include("QT564322"))
    }

    "return OK and empty dashboard when PSA associated to the scheme and dashboard has no entries" in {
      AuthStub.authorised()
      PensionSchemeStub.checkAssociationPsaTrue(srn.value)
      dashBoardSessionRepository.set(testDashboardData).futureValue
      TransferBackendStub.stubGetAllTransfersNotFound(pstr.value)

      val result =
        route(app, FakeRequest(routes.DashboardController.onPageLoad(1, None)).withSession(authSession)).value

      status(result) mustBe OK
      val savedRepo = dashBoardSessionRepository.get(testInternalId).futureValue.value
      savedRepo.get(TransfersOverviewQuery) mustBe Some(Seq.empty)
    }

    "redirect to recovery page when PSA is not associated to the scheme" in {
      AuthStub.authorised()
      PensionSchemeStub.checkAssociationPsaFalse(srn.value)
      dashBoardSessionRepository.set(testDashboardData).futureValue
      TransferBackendStub.stubGetAllTransfersOk(pstr.value, testNino)

      val result =
        route(app, FakeRequest(routes.DashboardController.onPageLoad(1, None)).withSession(authSession)).value

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe controllers.auth.routes.UnauthorisedController.onPageLoad().url
    }
  }

  "PsaDeclarationController.onSubmit" when {
    "a PSA submits a transfer" must {
      "submit, save the QT number, send the email and redirect" in {
        AuthStub.authorised()
        PensionSchemeStub.checkAssociationPsaTrue(srn.value)
        dashBoardSessionRepository.set(testDashboardData).futureValue
        sessionRepository.set(testSessionData).futureValue
        MinimalDetailsStub.psaSuccess(psaId.value, Json.toJson(minimalDetailsIndividual).toString)
        EmailStub.sendEmailResponse(OK, Json.toJson(emailRequest).toString)
        stubGet(
          s"/overseas-pension-transfer-backend/save-for-later/${testTransferNumber.value}",
          Json.toJson(UserAnswersDTO(testTransferNumber, pstr, testUserAnswerData, now)).toString()
        )
        stubPost(
          s"/overseas-pension-transfer-backend/submit-declaration/${testTransferNumber.value}",
          Json.toJson(SubmissionResponse(testQtNumber, now)).toString,
          OK
        )
        val result = route(
          app,
          FakeRequest(routes.PsaDeclarationController.onSubmit(NormalMode))
            .withSession(authSession)
        ).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.TransferSubmittedController.onPageLoad().url
      }
    }
  }
}

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

package controllers.viewandamend

import base.SpecBase
import models.{PstrNumber, QtNumber, QtStatus}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, reset, verify, when}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.SEE_OTHER
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.{LockService, UserAnswersService}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class ViewAmendSelectorControllerSpec
    extends AnyFreeSpec
    with SpecBase
    with MockitoSugar {

  private val mockUserAnswersService = mock[UserAnswersService]
  private val mockLockService        = mock[LockService]
  private val mockSessionRepository  = mock[SessionRepository]

  private val qtReference   = QtNumber("QT112233")
  override val pstr         = PstrNumber("87654321AB")
  private val qtStatus      = QtStatus.Submitted
  private val versionNumber = "1"

  private def buildApp = applicationBuilder(userAnswers = emptyUserAnswers)
    .overrides(
      bind[UserAnswersService].toInstance(mockUserAnswersService),
      bind[LockService].toInstance(mockLockService),
      bind[SessionRepository].toInstance(mockSessionRepository)
    ).build()

  "onPageLoad" - {

    "must release lock when a lock exists for the current user" in {
      when(mockLockService.isLocked(any(), any()))
        .thenReturn(Future.successful(true))

      when(mockLockService.releaseLock(any(), any()))
        .thenReturn(Future.unit)

      val app     = buildApp
      val request = FakeRequest(GET, routes.ViewAmendSelectorController.onPageLoad(qtReference, pstr, qtStatus, versionNumber).url)

      val result = route(app, request).value

      status(result) mustBe OK
      verify(mockLockService).releaseLock(any(), any())
      app.stop()
    }

    "must not release lock when no lock exists for the current user" in {

      reset(mockLockService)

      when(mockLockService.isLocked(any(), any()))
        .thenReturn(Future.successful(false))

      val app     = buildApp
      val request = FakeRequest(GET, routes.ViewAmendSelectorController.onPageLoad(qtReference, pstr, qtStatus, versionNumber).url)

      val result = route(app, request).value

      status(result) mustBe OK
      verify(mockLockService, never()).releaseLock(any(), any())
      app.stop()
    }
  }

  "onSubmit" - {
    "when 'View this overseas pension transfer' is selected on radio page" - {
      "user must be redirected to /view-submitted-transfer page" in {
        when(mockUserAnswersService.getExternalUserAnswers(any(), any(), any(), any())(any()))
          .thenReturn(Future.successful(Right(emptyUserAnswers)))

        val app     = buildApp
        val request = FakeRequest(POST, routes.ViewAmendSelectorController.onSubmit(qtReference, pstr, qtStatus, versionNumber).url)
          .withFormUrlEncodedBody("viewOrAmend" -> "view")

        val result = route(app, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.ViewAmendSubmittedController.view(qtReference, pstr, qtStatus, versionNumber).url
        app.stop()
      }
    }

    "when 'Amend this overseas pension transfer' is selected on radio page" - {
      "user must be redirected to /amend-submitted-transfer page when lock acquired successfully" in {
        when(mockUserAnswersService.getExternalUserAnswers(any(), any(), any(), any())(any()))
          .thenReturn(Future.successful(Right(emptyUserAnswers)))
        when(mockLockService.takeLockWithAudit(any(), any(), any(), any(), any(), any(), any())(any[HeaderCarrier]))
          .thenReturn(Future.successful(true))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val app     = buildApp
        val request = FakeRequest(POST, routes.ViewAmendSelectorController.onSubmit(qtReference, pstr, qtStatus, versionNumber).url)
          .withFormUrlEncodedBody("viewOrAmend" -> "amend")

        val result = route(app, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.ViewAmendSubmittedController.amend().url
        app.stop()
      }

      "must redirect with lock warning when lock cannot be acquired" in {
        when(mockUserAnswersService.getExternalUserAnswers(any(), any(), any(), any())(any()))
          .thenReturn(Future.successful(Right(emptyUserAnswers)))
        when(mockLockService.takeLockWithAudit(any(), any(), any(), any(), any(), any(), any())(any[HeaderCarrier]))
          .thenReturn(Future.successful(false))

        val app     = buildApp
        val request = FakeRequest(POST, routes.ViewAmendSelectorController.onSubmit(qtReference, pstr, qtStatus, versionNumber).url)
          .withFormUrlEncodedBody("viewOrAmend" -> "amend")

        val result = route(app, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.ViewAmendSelectorController.onPageLoad(qtReference, pstr, qtStatus, versionNumber).url
        flash(result).get("lockWarning") mustBe defined
        app.stop()
      }
    }

    "when no option is selected on view-amend page" - {
      "must show error message" in {
        val app     = buildApp
        val request = FakeRequest(POST, routes.ViewAmendSelectorController.onSubmit(qtReference, pstr, qtStatus, versionNumber).url)
          .withFormUrlEncodedBody("viewOrAmend" -> "")

        val result = route(app, request).value

        status(result) mustBe BAD_REQUEST
      }
    }
  }
}

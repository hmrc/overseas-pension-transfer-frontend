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

package controllers

import base.SpecBase
import models.{PstrNumber, QtStatus}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.SEE_OTHER
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.UserAnswersService
import uk.gov.hmrc.mongo.lock.{Lock, LockRepository}
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

class ViewAmendSelectorControllerSpec
  extends AnyFreeSpec
    with SpecBase
    with MockitoSugar {

  private val mockUserAnswersService = mock[UserAnswersService]
  private val mockLockRepository     = mock[LockRepository]
  private val mockSessionRepository  = mock[SessionRepository]

  private val qtReference   = "QU112233"
  override val pstr        = PstrNumber("87654321AB")
  private val qtStatus     = QtStatus.Submitted
  private val versionNumber = "1"

  private def buildApp = applicationBuilder(userAnswers = emptyUserAnswers)
    .overrides(
      bind[UserAnswersService].toInstance(mockUserAnswersService),
      bind[LockRepository].toInstance(mockLockRepository),
      bind[SessionRepository].toInstance(mockSessionRepository)
    ).build()

  "onSubmit" - {
    "when 'View this overseas pension transfer' is selected on radio page" - {
      "user must be redirected to /view-submitted-transfer page" in {
        when(mockUserAnswersService.getExternalUserAnswers(any(), any(), any(), any(), any())(any()))
          .thenReturn(Future.successful(Right(emptyUserAnswers)))

        val app = buildApp
        val request = FakeRequest(POST, routes.ViewAmendSelectorController.onSubmit(qtReference, pstr, qtStatus, versionNumber).url)
          .withFormUrlEncodedBody("option" -> "view")

        val result = route(app, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.ViewAmendSubmittedController.view(qtReference, pstr, qtStatus, versionNumber).url
        app.stop()
      }
    }

    "when 'Amend this overseas pension transfer' is selected on radio page" - {
      "user must be redirected to /amend-submitted-transfer page" in {
        when(mockUserAnswersService.getExternalUserAnswers(any(), any(), any(), any(), any())(any()))
          .thenReturn(Future.successful(Right(emptyUserAnswers)))
        when(mockLockRepository.takeLock(any[String], any[String], any[FiniteDuration]))
          .thenReturn(Future.successful(Some(mock[Lock])))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val app = buildApp
        val request = FakeRequest(POST, routes.ViewAmendSelectorController.onSubmit(qtReference, pstr, qtStatus, versionNumber).url)
          .withFormUrlEncodedBody("option" -> "amend")

        val result = route(app, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.ViewAmendSubmittedController.amend().url
        app.stop()
      }

      "must redirect with lock warning when lock cannot be acquired" in {
        when(mockUserAnswersService.getExternalUserAnswers(any(), any(), any(), any(), any())(any()))
          .thenReturn(Future.successful(Right(emptyUserAnswers)))
        when(mockLockRepository.takeLock(any[String], any[String], any[FiniteDuration]))
          .thenReturn(Future.successful(None))

        val app = buildApp
        val request = FakeRequest(POST, routes.ViewAmendSelectorController.onSubmit(qtReference, pstr, qtStatus, versionNumber).url)
          .withFormUrlEncodedBody("option" -> "amend")

        val result = route(app, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.ViewAmendSelectorController.onPageLoad(qtReference, pstr, qtStatus, versionNumber).url
        flash(result).get("lockWarning") mustBe defined
        app.stop()
      }
    }

    "when no option is selected on view-amend page" - {
      "must show error message" in {
        val app = buildApp
        val request = FakeRequest(POST, routes.ViewAmendSelectorController.onSubmit(qtReference, pstr, qtStatus, versionNumber).url)
          .withFormUrlEncodedBody("option" -> "")

        val result = route(app, request).value

        status(result) mustBe SEE_OTHER
        flash(result).get("error") mustBe Some("true")
        redirectLocation(result).value mustBe routes.ViewAmendSelectorController.onPageLoad(qtReference, pstr, qtStatus, versionNumber).url
        app.stop()
      }
    }
  }
}
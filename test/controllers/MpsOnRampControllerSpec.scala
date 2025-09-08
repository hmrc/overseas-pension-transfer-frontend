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
import connectors.PensionSchemeConnector
import models.responses.PensionSchemeErrorResponse
import models.{DashboardData, PensionSchemeDetails, PstrNumber, SrnNumber}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.PensionSchemeDetailsQuery
import repositories.DashboardSessionRepository

import scala.concurrent.Future

class MpsOnRampControllerSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  "MpsOnRampController onRamp" - {

    "must store PensionSchemeDetails in dashboard cache and redirect to next page when repo returns true" in {
      val mockRepo      = mock[DashboardSessionRepository]
      val mockConnector = mock[PensionSchemeConnector]

      val srn = "S2400000040"
      val psd = PensionSchemeDetails(SrnNumber(srn), PstrNumber("24000040IN"), "Open Scheme Overview API Test")

      when(mockConnector.getSchemeDetails(eqTo(srn), any())(any()))
        .thenReturn(Future.successful(Right(psd)))

      when(mockRepo.set(any[DashboardData]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = emptyUserAnswers)
          .overrides(
            bind[DashboardSessionRepository].toInstance(mockRepo),
            bind[PensionSchemeConnector].toInstance(mockConnector)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.MpsOnRampController.onRamp(srn).url)
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER

        val captor               = ArgumentCaptor.forClass(classOf[DashboardData])
        verify(mockRepo, times(1)).set(captor.capture())
        val saved: DashboardData = captor.getValue

        saved.get(PensionSchemeDetailsQuery) mustBe Some(psd)

        redirectLocation(result).value mustBe pages.MpsOnRampPage.nextPage(saved).url
      }
    }

    "must redirect to Journey Recovery when the connector returns an error" in {
      val mockRepo      = mock[DashboardSessionRepository]
      val mockConnector = mock[PensionSchemeConnector]

      val srn = "S2400000040"

      when(mockConnector.getSchemeDetails(eqTo(srn), any())(any()))
        .thenReturn(Future.successful(Left(PensionSchemeErrorResponse("BOOM", Some("bad json")))))

      val application =
        applicationBuilder(userAnswers = emptyUserAnswers)
          .overrides(
            bind[DashboardSessionRepository].toInstance(mockRepo),
            bind[PensionSchemeConnector].toInstance(mockConnector)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.MpsOnRampController.onRamp(srn).url)
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url

        verify(mockRepo, never()).set(any[DashboardData])
      }
    }

    "must redirect to Journey Recovery when the repo returns false" in {
      val mockRepo      = mock[DashboardSessionRepository]
      val mockConnector = mock[PensionSchemeConnector]

      val srn = "S2400000040"
      val psd = PensionSchemeDetails(SrnNumber(srn), PstrNumber("24000040IN"), "Open Scheme Overview API Test")

      when(mockConnector.getSchemeDetails(eqTo(srn), any())(any()))
        .thenReturn(Future.successful(Right(psd)))

      when(mockRepo.set(any[DashboardData]))
        .thenReturn(Future.successful(false))

      val application =
        applicationBuilder(userAnswers = emptyUserAnswers)
          .overrides(
            bind[DashboardSessionRepository].toInstance(mockRepo),
            bind[PensionSchemeConnector].toInstance(mockConnector)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.MpsOnRampController.onRamp(srn).url)
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when persisting to the repo fails" in {
      val mockRepo      = mock[DashboardSessionRepository]
      val mockConnector = mock[PensionSchemeConnector]

      val srn = "S2400000040"
      val psd = PensionSchemeDetails(SrnNumber(srn), PstrNumber("24000040IN"), "Open Scheme Overview API Test")

      when(mockConnector.getSchemeDetails(eqTo(srn), any())(any()))
        .thenReturn(Future.successful(Right(psd)))

      when(mockRepo.set(any[DashboardData]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val application =
        applicationBuilder(userAnswers = emptyUserAnswers)
          .overrides(
            bind[DashboardSessionRepository].toInstance(mockRepo),
            bind[PensionSchemeConnector].toInstance(mockConnector)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.MpsOnRampController.onRamp(srn).url)
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

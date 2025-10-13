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
import models.audit.JourneyStartedType.StartNewTransfer
import models.audit.{JsonAuditModel, ReportStartedAuditModel}
import models.{SessionData, UserAnswers}
import org.apache.pekko.Done
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.{AuditService, UserAnswersService}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent
import views.html.WhatWillBeNeededView

import scala.concurrent.Future

class WhatWillBeNeededControllerSpec
    extends AnyFreeSpec
    with SpecBase
    with MockitoSugar {

  "WhatWillBeNeededController.onPageLoad" - {

    "must initialise UserAnswers, persist once, and render the view when none exist" in {
      val mockRepo                                    = mock[SessionRepository]
      val mockUserAnswersService                      = mock[UserAnswersService]
      val mockAuditService                            = mock[AuditService]
      val eventCaptor: ArgumentCaptor[JsonAuditModel] = ArgumentCaptor.forClass(classOf[JsonAuditModel])

      when(mockUserAnswersService.setExternalUserAnswers(any[UserAnswers])(any())).thenReturn(Future.successful(Right(Done)))
      when(mockRepo.set(any[SessionData])).thenReturn(Future.successful(true))

      val application =
        applicationBuilder()
          .overrides(
            bind[SessionRepository].toInstance(mockRepo),
            bind[UserAnswersService].toInstance(mockUserAnswersService),
            bind[AuditService].toInstance(mockAuditService)
          ).build()

      running(application) {
        val request  = FakeRequest(GET, routes.WhatWillBeNeededController.onPageLoad().url)
        val result   = route(application, request).value
        val view     = application.injector.instanceOf[WhatWillBeNeededView]
        val nextPage = controllers.routes.TaskListController.onPageLoad().url

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(nextPage)(request, messages(application)).toString

        verify(mockRepo).set(any[SessionData])
        verify(mockAuditService, times(1)).audit(eventCaptor.capture())(any())

        val auditData: ReportStartedAuditModel = eventCaptor.getValue.asInstanceOf[ReportStartedAuditModel]
        auditData.auditType mustEqual "OverseasPensionTransferReportStarted"
        auditData.journey mustEqual StartNewTransfer
        auditData.allTransfersItem mustBe None
        auditData.failure mustBe None
      }
    }

    "must redirect to JourneyRecovery when persistence returns false" in {
      val mockRepo               = mock[SessionRepository]
      val mockUserAnswersService = mock[UserAnswersService]

      when(mockUserAnswersService.setExternalUserAnswers(any[UserAnswers])(any())).thenReturn(Future.successful(Right(Done)))
      when(mockRepo.set(any[SessionData])).thenReturn(Future.successful(false))

      val application =
        applicationBuilder()
          .overrides(
            bind[SessionRepository].toInstance(mockRepo),
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.WhatWillBeNeededController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

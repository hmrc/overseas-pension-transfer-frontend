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
import views.html.WhatWillBeNeededView

import scala.concurrent.Future

class WhatWillBeNeededControllerSpec
    extends AnyFreeSpec
    with SpecBase
    with MockitoSugar {

  "onPageLoad" - {
    "must render the view with the correct form action" in {
      val application = applicationBuilder().build()

      running(application) {
        val request = FakeRequest(GET, routes.WhatWillBeNeededController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[WhatWillBeNeededView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }
  }

  "onSubmit" - {
    "must initialise UserAnswers, persist once, audit, and redirect" in {
      val mockRepo                                    = mock[SessionRepository]
      val mockUserAnswerSvc                           = mock[UserAnswersService]
      val mockAuditService                            = mock[AuditService]
      val eventCaptor: ArgumentCaptor[JsonAuditModel] = ArgumentCaptor.forClass(classOf[JsonAuditModel])

      when(mockUserAnswerSvc.setExternalUserAnswers(any[UserAnswers])(any())).thenReturn(Future.successful(Right(Done)))
      when(mockRepo.set(any[SessionData])).thenReturn(Future.successful(true))

      val application =
        applicationBuilder()
          .overrides(
            bind[SessionRepository].toInstance(mockRepo),
            bind[UserAnswersService].toInstance(mockUserAnswerSvc),
            bind[AuditService].toInstance(mockAuditService)
          ).build()

      running(application) {
        val request = FakeRequest(POST, routes.WhatWillBeNeededController.onSubmit().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.TaskListController.onPageLoad().url

        verify(mockRepo, times(1)).set(any[SessionData])
        verify(mockAuditService, times(1)).audit(eventCaptor.capture())(any())

        val auditModel = eventCaptor.getValue.asInstanceOf[ReportStartedAuditModel]
        auditModel.journeyType mustEqual StartNewTransfer
        auditModel.auditType mustEqual "OverseasPensionTransferReportStarted"
      }
    }

    "must redirect to JourneyRecovery when persistence fails" in {
      val mockRepo          = mock[SessionRepository]
      val mockUserAnswerSvc = mock[UserAnswersService]

      when(mockUserAnswerSvc.setExternalUserAnswers(any[UserAnswers])(any())).thenReturn(Future.successful(Right(Done)))
      when(mockRepo.set(any[SessionData])).thenReturn(Future.successful(false))

      val application =
        applicationBuilder()
          .overrides(
            bind[SessionRepository].toInstance(mockRepo),
            bind[UserAnswersService].toInstance(mockUserAnswerSvc)
          ).build()

      running(application) {
        val request = FakeRequest(POST, routes.WhatWillBeNeededController.onSubmit().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

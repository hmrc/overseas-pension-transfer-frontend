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
import models.{DashboardData, PstrNumber, SrnNumber}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.mps.{PstrQuery, SrnQuery}
import repositories.DashboardSessionRepository
import views.html.DashboardView

import scala.concurrent.Future

class DashboardControllerSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  "DashboardController onPageLoad" - {

    "must return OK and render the view when dashboard data exists" in {
      val mockRepo = mock[DashboardSessionRepository]

      val dd = DashboardData(id = "ignore")
        .set(SrnQuery, SrnNumber("S1234567")).flatMap(
          _.set(PstrQuery, PstrNumber("12345678AB"))
        ).get

      when(mockRepo.get(any[String])).thenReturn(Future.successful(Some(dd)))

      val application =
        applicationBuilder(userAnswers = None)
          .overrides(bind[DashboardSessionRepository].toInstance(mockRepo))
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.DashboardController.onPageLoad().url)
        val result  = route(application, request).value

        val view         = application.injector.instanceOf[DashboardView]
        val expectedHtml = view(pages.DashboardPage.nextPage(dd).url)(request, messages(application)).toString

        status(result) mustBe OK
        contentAsString(result) mustBe expectedHtml
      }
    }

    "must redirect to Journey Recovery when no dashboard data exists" in {
      val mockRepo = mock[DashboardSessionRepository]
      when(mockRepo.get(any[String])).thenReturn(Future.successful(None))

      val application =
        applicationBuilder(userAnswers = None)
          .overrides(bind[DashboardSessionRepository].toInstance(mockRepo))
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.DashboardController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

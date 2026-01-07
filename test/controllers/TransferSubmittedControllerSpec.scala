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
import config.FrontendAppConfig
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import utils.DateTimeFormats.localDateTimeFormatter
import viewmodels.checkAnswers.TransferSubmittedSummary
import views.html.TransferSubmittedView

import java.time.ZoneId
import scala.concurrent.Future

class TransferSubmittedControllerSpec extends AnyFreeSpec with SpecBase {

  private val mockSessionRepository = mock[SessionRepository]
  private val application           = new GuiceApplicationBuilder().build()
  private val appConfig             = application.injector.instanceOf[FrontendAppConfig]

  "TransferSubmitted Controller" - {

    "must return OK and the correct view for a GET" in {
      val application  = applicationBuilder(sessionData = sessionDataMemberNameQtNumberTransferSubmitted)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()
      val testMessages = messages(application)

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(sessionDataMemberNameQtNumberTransferSubmitted)))

      running(application) {
        val request = FakeRequest(GET, routes.TransferSubmittedController.onPageLoad().url)

        val result = route(application, request).value

        val expectedMpsLink = s"${appConfig.pensionSchemeSummaryUrl}1234567890"

        val view = application.injector.instanceOf[TransferSubmittedView]

        val formattedInstant = {
          val dateTime = testDateTransferSubmitted.atZone(ZoneId.systemDefault()).toLocalDateTime
          dateTime.format(localDateTimeFormatter)
        }

        val summaryList =
          TransferSubmittedSummary.rows(
            "User McUser",
            formattedInstant
          )(
            fakeSchemeRequest(request),
            testMessages
          )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view("QT123456", summaryList, expectedMpsLink, appConfig)(
          fakeSchemeRequest(request),
          testMessages
        ).toString
      }
    }

    "redirect to JourneyRecovery page when the sessionRepo is empty" in {
      val application = applicationBuilder(sessionData = sessionDataMemberNameQtNumberTransferSubmitted)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))

      running(application) {
        val request = FakeRequest(GET, routes.TransferSubmittedController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }
    }
  }
}

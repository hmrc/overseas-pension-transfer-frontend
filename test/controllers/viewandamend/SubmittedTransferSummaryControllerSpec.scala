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
import controllers.viewandamend.routes
import models.PstrNumber
import models.QtStatus.Submitted
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status.OK
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.CollectSubmittedVersionsService
import viewmodels.SubmittedTransferSummaryViewModel
import views.html.viewandamend.SubmittedTransferSummaryView

import scala.concurrent.Future

class SubmittedTransferSummaryControllerSpec extends AnyFreeSpec with SpecBase {

  private val mockCollectVersionsService = mock[CollectSubmittedVersionsService]

  "onPageLoad" - {
    "Return 200 with table content" in {
      val application  = applicationBuilder(userAnswers = emptyUserAnswers)
        .overrides(
          bind[CollectSubmittedVersionsService].toInstance(mockCollectVersionsService)
        )
        .build()
      val testMessages = messages(application)

      when(mockCollectVersionsService.collectVersions(any(), any(), any(), any())(any()))
        .thenReturn(Future.successful(None, List(emptyUserAnswers)))

      running(application) {
        val request = FakeRequest(GET, routes.SubmittedTransferSummaryController.onPageLoad(testQtNumber, PstrNumber("12345678AB"), Submitted, "001").url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubmittedTransferSummaryView]

        val summaryList =
          SubmittedTransferSummaryViewModel.rows(None, List(emptyUserAnswers), "001")(testMessages)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view("", testQtNumber.value, summaryList)(fakeSchemeRequest(request), testMessages).toString
      }
    }
  }
}

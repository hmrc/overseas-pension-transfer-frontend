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

package controllers.transferDetails

import base.SpecBase
import models.{AmendCheckMode, CheckMode}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.checkAnswers.transferDetails.TransferDetailsSummary
import viewmodels.govuk.all.SummaryListViewModel
import views.html.transferDetails.TransferDetailsCYAView

class TransferDetailsCYAControllerSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  private lazy val onPageLoadRoute = controllers.transferDetails.routes.TransferDetailsCYAController.onPageLoad().url

  private lazy val onSubmitRoute = controllers.transferDetails.routes.TransferDetailsCYAController.onSubmit().url

  "TransferDetailsCYAController" - {

    "onPageLoad" - {

      "must return OK and render the TransferDetailsCYA view" in {
        val userAnswers = emptyUserAnswers
        val application = applicationBuilder(userAnswers = userAnswers).build()

        running(application) {
          val request = FakeRequest(GET, onPageLoadRoute)
          val result  = route(application, request).value

          val view                    = application.injector.instanceOf[TransferDetailsCYAView]
          implicit val msgs: Messages = messages(application)

          val summaryList = SummaryListViewModel(
            TransferDetailsSummary.rows(CheckMode, userAnswers)
          )

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(summaryList)(fakeDisplayRequest(request, userAnswers), msgs).toString
        }
      }
    }

    "onSubmit" - {

      "must redirect using CheckMode for a new transfer journey" in {
        val userAnswers = emptyUserAnswers
        val application = applicationBuilder(userAnswers = userAnswers).build()

        running(application) {
          val request = FakeRequest(POST, onSubmitRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            pages.transferDetails.TransferDetailsSummaryPage
              .nextPage(CheckMode, userAnswers)
              .url
        }
      }

      "must redirect using AmendCheckMode when versionNumber is present" in {
        val userAnswers = emptyUserAnswers
        val sessionData = emptySessionData.copy(
          data = Json.obj("versionNumber" -> "001")
        )
        val application = applicationBuilder(userAnswers = userAnswers, sessionData = sessionData).build()

        running(application) {
          val request = FakeRequest(POST, onSubmitRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            pages.transferDetails.TransferDetailsSummaryPage
              .nextPage(AmendCheckMode, userAnswers)
              .url
        }
      }

      "must redirect using AmendCheckMode when receiptDate is present" in {
        val userAnswers = emptyUserAnswers
        val sessionData = emptySessionData.copy(
          data = Json.obj("receiptDate" -> "2025-12-16T16:43:33.984Z")
        )
        val application = applicationBuilder(userAnswers = userAnswers, sessionData = sessionData).build()

        running(application) {
          val request = FakeRequest(POST, onSubmitRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            pages.transferDetails.TransferDetailsSummaryPage
              .nextPage(AmendCheckMode, userAnswers)
              .url
        }
      }
    }
  }
}

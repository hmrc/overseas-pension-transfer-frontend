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

package controllers.qropsSchemeManagerDetails

import base.SpecBase
import models.CheckMode
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.checkAnswers.qropsSchemeManagerDetails.SchemeManagerDetailsSummary
import viewmodels.govuk.all.SummaryListViewModel
import views.html.qropsSchemeManagerDetails.SchemeManagerDetailsCYAView

class SchemeManagerDetailsCYAControllerSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  private lazy val onPageLoadRoute = controllers.qropsSchemeManagerDetails.routes.SchemeManagerDetailsCYAController.onPageLoad().url
  private lazy val onSubmitRoute   = controllers.qropsSchemeManagerDetails.routes.SchemeManagerDetailsCYAController.onSubmit().url

  "SchemeManagerDetailsCYAController" - {

    "onPageLoad" - {

      "must return OK and render the SchemeManagerDetailsCYA view with correct summary list" in {
        val userAnswers = emptyUserAnswers
        val application = applicationBuilder(userAnswers = userAnswers).build()

        running(application) {
          val request = FakeRequest(GET, onPageLoadRoute)
          val result  = route(application, request).value

          val view                    = application.injector.instanceOf[SchemeManagerDetailsCYAView]
          implicit val msgs: Messages = messages(application)

          val summaryList = SummaryListViewModel(
            SchemeManagerDetailsSummary.rows(CheckMode, userAnswers)
          )

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(summaryList)(fakeDisplayRequest(request, userAnswers), msgs).toString
        }
      }
    }

    "onSubmit" - {

      "must redirect to the task list page" in {
        val userAnswers = emptyUserAnswers
        val application = applicationBuilder(userAnswers = userAnswers).build()

        running(application) {
          val request = FakeRequest(POST, onSubmitRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.TaskListController.onPageLoad().url
        }
      }
    }
  }
}

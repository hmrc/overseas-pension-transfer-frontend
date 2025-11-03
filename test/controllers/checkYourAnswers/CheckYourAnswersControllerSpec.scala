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

package controllers.checkYourAnswers

import base.SpecBase
import models.{FinalCheckMode, NormalMode}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.OK
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, route, status, writeableOf_AnyContentAsEmpty, GET}
import viewmodels.checkAnswers.transferDetails.TransferDetailsSummary
import viewmodels.govuk.SummaryListFluency
import viewmodels.govuk.all.SummaryListViewModel
import views.html.checkYourAnswers.CheckYourAnswersView

class CheckYourAnswersControllerSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  private val application = applicationBuilder().build()

  private lazy val checkYourAnswersRoute = routes.CheckYourAnswersController.onPageLoad().url

  val memberDetailsSummaryList        = SummaryListViewModel(Seq.empty)
  val transferDetailsSummaryList      = SummaryListViewModel(TransferDetailsSummary.rows(FinalCheckMode, emptyUserAnswers)(messages(application)))
  val qropsDetailsSummaryList         = SummaryListViewModel(Seq.empty)
  val schemeManagerDetailsSummaryList = SummaryListViewModel(Seq.empty)

  "CheckYourAnswersController" - {
    "onPageLoad" - {
      "return Ok and correct view" in {

        val request = FakeRequest(GET, checkYourAnswersRoute)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]

        status(result) mustBe OK
        contentAsString(result) mustBe view(
          memberDetailsSummaryList,
          transferDetailsSummaryList,
          qropsDetailsSummaryList,
          schemeManagerDetailsSummaryList,
          NormalMode
        )(
          fakeDisplayRequest(request),
          messages(application)
        ).toString
      }
    }
  }

}

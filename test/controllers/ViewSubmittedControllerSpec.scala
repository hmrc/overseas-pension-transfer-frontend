/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
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
import models.FinalCheckMode
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.OK
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, route, status, writeableOf_AnyContentAsEmpty, GET}
import viewmodels.checkAnswers.memberDetails.MemberDetailsSummary
import viewmodels.checkAnswers.qropsDetails.QROPSDetailsSummary
import viewmodels.checkAnswers.qropsSchemeManagerDetails.SchemeManagerDetailsSummary
import viewmodels.checkAnswers.schemeOverview.SchemeDetailsSummary
import viewmodels.checkAnswers.transferDetails.TransferDetailsSummary
import viewmodels.govuk.SummaryListFluency
import views.html.ViewSubmittedView

class ViewSubmittedControllerSpec extends AnyFreeSpec with SpecBase with MockitoSugar with SummaryListFluency {

  private val application =
    applicationBuilder(
      userAnswers = userAnswersMemberNameQtNumberTransferSubmitted,
      sessionData = sessionDataQtNumberTransferSubmitted
    ).build()

  private lazy val submittedRoute = routes.ViewSubmittedController.onPageLoad(
    qtNumber      = testQtNumber.value,
    pstr          = pstr.value,
    qtStatus      = "Submitted",
    versionNumber = "7",
    dateSubmitted = formattedTestDateTransferSubmitted
  ).url

  private val schemeSummaryList =
    SummaryListViewModel(
      SchemeDetailsSummary.rows(
        FinalCheckMode,
        schemeDetails.schemeName,
        formattedTestDateTransferSubmitted
      )(messages(application))
    )

  private val memberDetailsSummaryList =
    SummaryListViewModel(
      MemberDetailsSummary.rows(
        FinalCheckMode,
        userAnswersMemberNameQtNumberTransferSubmitted,
        showChangeLinks = false
      )(messages(application))
    )

  private val transferDetailsSummaryList =
    SummaryListViewModel(
      TransferDetailsSummary.rows(
        FinalCheckMode,
        userAnswersMemberNameQtNumberTransferSubmitted,
        showChangeLinks = false
      )(messages(application))
    )

  private val qropsDetailsSummaryList =
    SummaryListViewModel(
      QROPSDetailsSummary.rows(
        FinalCheckMode,
        userAnswersMemberNameQtNumberTransferSubmitted,
        showChangeLinks = false
      )(messages(application))
    )

  private val schemeManagerDetailsSummaryList =
    SummaryListViewModel(
      SchemeManagerDetailsSummary.rows(
        FinalCheckMode,
        userAnswersMemberNameQtNumberTransferSubmitted,
        showChangeLinks = false
      )(messages(application))
    )

  "ViewSubmittedController" - {
    "onPageLoad" - {
      "return Ok and render the expected view with five SummaryListViewModels" in {
        val request = FakeRequest(GET, submittedRoute)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[ViewSubmittedView]

        status(result) mustBe OK
        contentAsString(result) mustBe view(
          schemeSummaryList,
          memberDetailsSummaryList,
          transferDetailsSummaryList,
          qropsDetailsSummaryList,
          schemeManagerDetailsSummaryList
        )(
          fakeDisplayRequest(
            request,
            userAnswersMemberNameQtNumberTransferSubmitted,
            sessionDataQtNumberTransferSubmitted
          ),
          messages(application)
        ).toString
      }
    }
  }
}

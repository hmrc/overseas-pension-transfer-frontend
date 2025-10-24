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

package pages

import base.SpecBase
import controllers.routes
import models.QtStatus.{InProgress, Submitted}
import models.{DashboardData, PensionSchemeDetails, QtNumber, SrnNumber, TransferReportQueryParams}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.mvc.Call
import queries.PensionSchemeDetailsQuery

class DashboardPageSpec extends AnyFreeSpec with Matchers with SpecBase {

  private val scheme   = PensionSchemeDetails(SrnNumber("S1234567"), pstr, "Scheme Name")
  private val internal = "internal-id"

  private def ddWithScheme: DashboardData =
    DashboardData(internal).set(PensionSchemeDetailsQuery, scheme).success.value

  private def ddEmpty: DashboardData =
    DashboardData(internal)

  private def inProgressParams(): TransferReportQueryParams =
    TransferReportQueryParams(
      transferId    = Some(userAnswersTransferNumber),
      qtStatus      = Some(InProgress),
      pstr          = None,
      versionNumber = None,
      memberName    = "Name",
      currentPage   = 1
    )

  private def submittedParams(qtRef: String = "QT123456", ver: String = "007"): TransferReportQueryParams =
    TransferReportQueryParams(
      transferId    = Some(QtNumber(qtRef)),
      qtStatus      = Some(Submitted),
      pstr          = Some(pstr),
      versionNumber = Some(ver),
      memberName    = "Name",
      currentPage   = 1
    )

  ".nextPage" - {

    "must go to WhatWillBeNeeded when PensionSchemeDetails exists and no status provided" in {
      DashboardPage.nextPage(ddWithScheme, None, None) mustEqual
        routes.WhatWillBeNeededController.onPageLoad()
    }

    "must go to Unauthorised when PensionSchemeDetails is missing and no status provided" in {
      DashboardPage.nextPage(ddEmpty, None, None) mustEqual
        controllers.auth.routes.UnauthorisedController.onPageLoad()
    }

    "must go to TaskListController when status is InProgress and transferReference exists" in {
      val call: Call =
        DashboardPage.nextPage(ddEmpty, Some(InProgress), Some(inProgressParams()))

      call mustEqual controllers.routes.TaskListController.fromDashboard(userAnswersTransferNumber)
    }

    "must go to JourneyRecovery when status is InProgress but transferReference is missing" in {
      val paramsMissingRef = TransferReportQueryParams(
        transferId    = None,
        qtStatus      = Some(InProgress),
        pstr          = None,
        versionNumber = None,
        memberName    = "Name",
        currentPage   = 1
      )

      DashboardPage.nextPage(ddEmpty, Some(InProgress), Some(paramsMissingRef)) mustEqual
        controllers.routes.JourneyRecoveryController.onPageLoad()
    }

    "must go to ViewSubmittedController when status is Submitted and required params are present" in {
      val p    = submittedParams("QT123654")
      val call = DashboardPage.nextPage(ddEmpty, Some(Submitted), Some(p))

      call mustEqual controllers.viewandamend.routes.SubmittedTransferSummaryController.onPageLoad(
        QtNumber("QT123654"),
        pstr,
        Submitted,
        "007"
      )
    }

    "must go to JourneyRecovery when status is Submitted but any required param is missing" in {
      val missingVer  = submittedParams("QT654321").copy(versionNumber = None)
      val missingPstr = submittedParams("QT654321").copy(pstr = None)
      val missingQt   = submittedParams("QT654321").copy(transferId = None)

      DashboardPage.nextPage(ddEmpty, Some(Submitted), Some(missingVer)) mustEqual
        controllers.routes.JourneyRecoveryController.onPageLoad()

      DashboardPage.nextPage(ddEmpty, Some(Submitted), Some(missingPstr)) mustEqual
        controllers.routes.JourneyRecoveryController.onPageLoad()

      DashboardPage.nextPage(ddEmpty, Some(Submitted), Some(missingQt)) mustEqual
        controllers.routes.JourneyRecoveryController.onPageLoad()
    }
  }
}

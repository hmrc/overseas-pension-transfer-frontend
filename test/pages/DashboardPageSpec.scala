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

import controllers.routes
import models.QtStatus.InProgress
import models.{DashboardData, PensionSchemeDetails, PstrNumber, QtStatus, SrnNumber, TransferReportQueryParams}
import org.scalatest.TryValues._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import queries.PensionSchemeDetailsQuery

class DashboardPageSpec extends AnyFreeSpec with Matchers {

  val transferReportQueryParams = TransferReportQueryParams(
    None,
    None,
    None,
    "Name",
    1
  )

  ".nextPage" - {

    "must go to WhatWillBeNeeded when PensionSchemeDetails exists and no status provided" in {
      val dd = DashboardData("internal-id")
        .set(PensionSchemeDetailsQuery, PensionSchemeDetails(SrnNumber("S1234567"), PstrNumber("12345678AB"), "Scheme Name"))
        .success
        .value

      DashboardPage.nextPage(dd, None, None) mustEqual routes.WhatWillBeNeededController.onPageLoad()
    }

    "must go to Unauthorised when PensionSchemeDetails is missing and no status provided" in {
      val dd = DashboardData("internal-id")

      DashboardPage.nextPage(dd, None, None) mustEqual controllers.auth.routes.UnauthorisedController.onPageLoad()
    }

    "must go to TransferProgressController when status is InProgress" in {
      val dd = DashboardData("internal-id")

      DashboardPage.nextPage(dd, Some(InProgress), Some("TR001")) mustEqual
        controllers.routes.TaskListController.fromDashboard("TR001")
    }

    "must go to JourneyRecovery when status is InProgress and no transferReference is found" in {
      val dd = DashboardData("internal-id")

      DashboardPage.nextPage(dd, Some(InProgress), None) mustEqual
        controllers.routes.JourneyRecoveryController.onPageLoad()
    }
//
//    "must go to TransferSummaryController when status is Compiled" in {
//      val dd = DashboardData("internal-id")
//
//      DashboardPage.nextPage(dd, Some(QtStatus.Compiled)) mustEqual ??? // TODO: Replace with Compiled controller redirect
//    }
//
//    "must go to TransferSummaryController when status is Submitted" in {
//      val dd = DashboardData("internal-id")
//
//      DashboardPage.nextPage(dd, Some(QtStatus.Submitted)) mustEqual ??? // TODO: Replace with Submitted controller redirect
//    }
  }
}

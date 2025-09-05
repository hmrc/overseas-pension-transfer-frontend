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
import models.{DashboardData, SrnNumber}
import org.scalatest.TryValues._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import queries.mps.SrnQuery

class DashboardPageSpec extends AnyFreeSpec with Matchers {

  ".nextPage" - {

    "must go to WhatWillBeNeeded when SRN exists" in {
      val dd = DashboardData("internal-id")
        .set(SrnQuery, SrnNumber("S1234567"))
        .success
        .value

      DashboardPage.nextPage(dd) mustEqual routes.WhatWillBeNeededController.onPageLoad()
    }

    "must go to Unauthorised when SRN is missing" in {
      val dd = DashboardData("internal-id")

      DashboardPage.nextPage(dd) mustEqual controllers.auth.routes.UnauthorisedController.onPageLoad()
    }
  }
}

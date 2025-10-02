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

package navigators

import base.SpecBase
import models.AllTransfersItem
import models.QtStatus.{Compiled, InProgress, Submitted}
import org.scalatest.freespec.AnyFreeSpec

class AllTransfersLinkNavigatorSpec extends AnyFreeSpec with SpecBase {

  private def itemWith(status: Option[models.QtStatus]): AllTransfersItem =
    AllTransfersItem(
      transferReference = None,
      qtReference       = None,
      qtVersion         = None,
      nino              = None,
      memberFirstName   = None,
      memberSurname     = None,
      submissionDate    = None,
      lastUpdated       = None,
      qtStatus          = status,
      pstrNumber        = None
    )

  "fallback goes to dashboard page 1 (default route)" in {
    val item = itemWith(None)
    AllTransfersLinkNavigator.linkFor(item).url mustBe
      controllers.routes.DashboardController.onPageLoad().url
  }

  "in-progress routes to JourneyRecovery (placeholder)" in {
    val item = itemWith(Some(InProgress))
    AllTransfersLinkNavigator.linkFor(item).url mustBe
      controllers.routes.JourneyRecoveryController.onPageLoad().url
  }

  "submitted routes to JourneyRecovery (placeholder)" in {
    val item = itemWith(Some(Submitted))
    AllTransfersLinkNavigator.linkFor(item).url mustBe
      controllers.routes.JourneyRecoveryController.onPageLoad().url
  }

  "compiled routes to JourneyRecovery (placeholder)" in {
    val item = itemWith(Some(Compiled))
    AllTransfersLinkNavigator.linkFor(item).url mustBe
      controllers.routes.JourneyRecoveryController.onPageLoad().url
  }
}

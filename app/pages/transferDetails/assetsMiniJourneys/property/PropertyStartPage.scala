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

package pages.transferDetails.assetsMiniJourneys.property

import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import models.{AmendCheckMode, CheckMode, FinalCheckMode, NormalMode, UserAnswers}
import pages.Page
import play.api.mvc.Call

object PropertyStartPage extends Page {
  private val startIndex = 0

  override protected def nextPageNormalMode(answers: UserAnswers): Call =
    AssetsMiniJourneysRoutes.PropertyAddressController.onPageLoad(NormalMode, startIndex)

  override protected def nextPageCheckMode(answers: UserAnswers): Call =
    AssetsMiniJourneysRoutes.PropertyAddressController.onPageLoad(CheckMode, startIndex)

  override protected def nextPageFinalCheckMode(answers: UserAnswers): Call =
    AssetsMiniJourneysRoutes.PropertyAddressController.onPageLoad(FinalCheckMode, startIndex)

  override protected def nextPageAmendCheckMode(answers: UserAnswers): Call =
    AssetsMiniJourneysRoutes.PropertyAddressController.onPageLoad(AmendCheckMode, startIndex)
}

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

package pages.transferDetails.assetsMiniJourneys.unquotedShares

import controllers.routes
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import models.{NormalMode, UserAnswers}
import pages.Page
import play.api.mvc.Call

object UnquotedSharesStartPage extends Page {

  private val startIndex = 0

  override protected def nextPageNormalMode(answers: UserAnswers): Call =
    AssetsMiniJourneysRoutes.UnquotedSharesCompanyNameController.onPageLoad(NormalMode, startIndex)

  override protected def nextPageCheckMode(answers: UserAnswers): Call =
    routes.JourneyRecoveryController.onPageLoad()
}

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

import models.{Mode, UserAnswers}
import pages.Page
import pages.transferDetails.TypeOfAssetPage
import play.api.mvc.Call
import services.TransferDetailsService
import javax.inject.Inject

class TypeOfAssetNavigator @Inject() (
    transferDetailsService: TransferDetailsService
  ) {

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = page match {
    case TypeOfAssetPage =>
      transferDetailsService.getNextAssetRoute(userAnswers)
        .getOrElse(controllers.routes.IndexController.onPageLoad())

    case _ =>
      controllers.routes.JourneyRecoveryController.onPageLoad()
  }
}

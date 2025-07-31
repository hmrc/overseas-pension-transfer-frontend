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

<<<<<<<< HEAD:test/pages/transferDetails/assetsMiniJourneys/unquotedShares/UnquotedSharesStartPageSpec.scala
package pages.transferDetails.assetsMiniJourney.unquotedShares
========
package pages.transferDetails.assetsMiniJourneys.quotedShares
>>>>>>>> 554ae0f (OAOTC-1259 start to refactor property mini journey and conform all assetsMiniJourney packages to assetsMiniJourneys name scheme):test/pages/transferDetails/assetsMiniJourneys/quotedShares/RemoveQuotedSharesPageSpec.scala

import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import models.{NormalMode, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class UnquotedSharesStartPageSpec extends AnyFreeSpec with Matchers {

  ".nextPage" - {

    val emptyAnswers = UserAnswers("id")

    "in Normal Mode" - {

      "must go to the Next page" in {
        UnquotedSharesStartPage.nextPage(NormalMode, emptyAnswers) mustEqual AssetsMiniJourneysRoutes.UnquotedSharesCompanyNameController.onPageLoad(
          mode  = NormalMode,
          index = 0
        )
      }
    }
  }
}

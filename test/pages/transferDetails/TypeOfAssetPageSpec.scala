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

package pages.transferDetails

import base.SpecBase
import controllers.transferDetails.routes
import models.{CheckMode, FinalCheckMode, NormalMode, PstrNumber, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class TypeOfAssetPageSpec extends AnyFreeSpec with Matchers with SpecBase {

  ".nextPage" - {

    val emptyAnswers = UserAnswers(userAnswersTransferNumber, PstrNumber("12345678AB"))

    "in Normal Mode" - {

      "must go to the Next page" in {
        TypeOfAssetPage.nextPage(NormalMode, emptyAnswers) mustEqual controllers.routes.DashboardController.onPageLoad()
      }
    }

    "in Check Mode" - {

      "must go to Check Answers" in {

        TypeOfAssetPage.nextPage(CheckMode, emptyAnswers) mustEqual routes.TransferDetailsCYAController.onPageLoad()
      }
    }

    "in FinalCheckMode" - {
      "must go to Final Check Answers page" in {
        TypeOfAssetPage.nextPage(FinalCheckMode, emptyAnswers) mustEqual
          controllers.checkYourAnswers.routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}

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

package pages.qropsSchemeManagerDetails

import controllers.qropsSchemeManagerDetails.routes
import models.{CheckMode, FinalCheckMode, NormalMode, SchemeManagerType, UserAnswers}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class SchemeManagerTypePageSpec extends AnyFreeSpec with Matchers {

  ".nextPage" - {

    val emptyAnswers = UserAnswers("id")

    "in Normal Mode" - {

      "must go to Manger's name page when the type is 'Individual'" in {

        SchemeManagerTypePage.nextPage(
          NormalMode,
          emptyAnswers.set(SchemeManagerTypePage, SchemeManagerType.Individual).success.value
        ) mustEqual routes.SchemeManagersNameController.onPageLoad(NormalMode)
      }

      "must go to Organisation name page when the type is 'Organisation'" in {

        SchemeManagerTypePage.nextPage(
          NormalMode,
          emptyAnswers.set(SchemeManagerTypePage, SchemeManagerType.Organisation).success.value
        ) mustEqual routes.SchemeManagerOrganisationNameController.onPageLoad(NormalMode)
      }
    }

    "in Check Mode" - {

      "must go to Check Answers" in {

        SchemeManagerTypePage.nextPage(CheckMode, emptyAnswers) mustEqual routes.SchemeManagerDetailsCYAController.onPageLoad()
      }
    }

    "in FinalCheckMode" - {
      "must go to Final Check Answers page" in {
        SchemeManagerTypePage.nextPage(FinalCheckMode, emptyAnswers) mustEqual
          controllers.checkYourAnswers.routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}

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
import models.{CheckMode, FinalCheckMode, NormalMode, PersonName, UserAnswers}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class SchemeManagerOrganisationNamePageSpec extends AnyFreeSpec with Matchers {

  ".nextPage" - {

    val emptyAnswers = UserAnswers("id")

    "in Normal Mode" - {

      "must go to Organisation individual name page" in {

        SchemeManagerOrganisationNamePage.nextPage(NormalMode, emptyAnswers) mustEqual routes.SchemeManagerOrgIndividualNameController.onPageLoad(NormalMode)
      }
    }

    "in Check Mode" - {

      "must go to Organisation individual name page in CheckMode" in {

        SchemeManagerOrganisationNamePage.nextPage(CheckMode, emptyAnswers) mustEqual routes.SchemeManagerOrgIndividualNameController.onPageLoad(CheckMode)
      }

      "must go to CYA if Organisation individual name exists in mongo" in {
        val ua = emptyAnswers.set(SchemeManagerOrgIndividualNamePage, PersonName("Bill", "Withers")).success.value
        SchemeManagerOrganisationNamePage.nextPage(CheckMode, ua) mustEqual routes.SchemeManagerDetailsCYAController.onPageLoad()
      }
    }

    "in FinalCheckMode" - {
      "must go to Final Check Answers page" in {
        SchemeManagerOrganisationNamePage.nextPage(FinalCheckMode, emptyAnswers) mustEqual
          controllers.checkYourAnswers.routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}

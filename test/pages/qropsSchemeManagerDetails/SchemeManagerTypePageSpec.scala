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

import base.SpecBase
import controllers.qropsSchemeManagerDetails.routes
import models.{AmendCheckMode, CheckMode, FinalCheckMode, NormalMode, PersonName, PstrNumber, SchemeManagerType, UserAnswers}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class SchemeManagerTypePageSpec extends AnyFreeSpec with Matchers with SpecBase {

  ".nextPage" - {

    val emptyAnswers = UserAnswers(userAnswersTransferNumber, PstrNumber("12345678AB"))

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

      "must go to Manger's name page in CheckMode when the type is 'Individual'" in {

        SchemeManagerTypePage.nextPage(
          CheckMode,
          emptyAnswers.set(SchemeManagerTypePage, SchemeManagerType.Individual).success.value
        ) mustEqual routes.SchemeManagersNameController.onPageLoad(CheckMode)
      }

      "must go to Organisation name page when the type is 'Organisation'" in {

        SchemeManagerTypePage.nextPage(
          CheckMode,
          emptyAnswers.set(SchemeManagerTypePage, SchemeManagerType.Organisation).success.value
        ) mustEqual routes.SchemeManagerOrganisationNameController.onPageLoad(CheckMode)
      }
    }

    "in Final Check Mode" - {

      "must go to Manger's name page in FinalCheckMode when the type is 'Individual'" in {

        SchemeManagerTypePage.nextPage(
          FinalCheckMode,
          emptyAnswers.set(SchemeManagerTypePage, SchemeManagerType.Individual).success.value
        ) mustEqual routes.SchemeManagersNameController.onPageLoad(FinalCheckMode)
      }

      "must go to Organisation name page when the type is 'Organisation'" in {

        SchemeManagerTypePage.nextPage(
          FinalCheckMode,
          emptyAnswers.set(SchemeManagerTypePage, SchemeManagerType.Organisation).success.value
        ) mustEqual routes.SchemeManagerOrganisationNameController.onPageLoad(FinalCheckMode)
      }
    }

    "in Amend Check Mode" - {

      "must go to Manger's name page in AmendCheckMode when the type is 'Individual'" in {

        SchemeManagerTypePage.nextPage(
          AmendCheckMode,
          emptyAnswers.set(SchemeManagerTypePage, SchemeManagerType.Individual).success.value
        ) mustEqual routes.SchemeManagersNameController.onPageLoad(AmendCheckMode)
      }

      "must go to Organisation name page when the type is 'Organisation'" in {

        SchemeManagerTypePage.nextPage(
          AmendCheckMode,
          emptyAnswers.set(SchemeManagerTypePage, SchemeManagerType.Organisation).success.value
        ) mustEqual routes.SchemeManagerOrganisationNameController.onPageLoad(AmendCheckMode)
      }
    }
  }

  "cleanup" - {

    val emptyAnswers   = UserAnswers(userAnswersTransferNumber, PstrNumber("12345678AB"))
    val individualName = PersonName("Ada", "Lovelace")
    val orgName        = "Analytical Engines Ltd"
    val orgContact     = PersonName("Charles", "Babbage")

    "must remove SchemeManagersNamePage when SchemeManagerType is Organisation" in {
      val withBoth =
        emptyAnswers
          .set(SchemeManagersNamePage, individualName).success.value
          .set(SchemeManagerOrganisationNamePage, orgName).success.value
          .set(SchemeManagerOrgIndividualNamePage, orgContact).success.value

      val cleaned = SchemeManagerTypePage.cleanup(Some(SchemeManagerType.Organisation), withBoth).success.value

      cleaned.get(SchemeManagersNamePage) mustBe None
      cleaned.get(SchemeManagerOrganisationNamePage) mustBe Some(orgName)
      cleaned.get(SchemeManagerOrgIndividualNamePage) mustBe Some(orgContact)
    }

    "must remove SchemeManagerOrganisationNamePage and SchemeManagerOrgIndividualNamePage when SchemeManagerType is Individual" in {
      val withBoth =
        emptyAnswers
          .set(SchemeManagersNamePage, individualName).success.value
          .set(SchemeManagerOrganisationNamePage, orgName).success.value
          .set(SchemeManagerOrgIndividualNamePage, orgContact).success.value

      val cleaned = SchemeManagerTypePage.cleanup(Some(SchemeManagerType.Individual), withBoth).success.value

      cleaned.get(SchemeManagerOrganisationNamePage) mustBe None
      cleaned.get(SchemeManagerOrgIndividualNamePage) mustBe None
      cleaned.get(SchemeManagersNamePage) mustBe Some(individualName)
    }
  }
}

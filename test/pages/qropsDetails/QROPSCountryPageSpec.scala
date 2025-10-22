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

package pages.qropsDetails

import base.SpecBase
import controllers.qropsDetails.routes
import models.address.Country
import models.{CheckMode, FinalCheckMode, NormalMode, PstrNumber, UserAnswers}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

class QROPSCountryPageSpec extends AnyFreeSpec with Matchers with SpecBase {

  private val emptyAnswers = UserAnswers(userAnswersTransferNumber, PstrNumber("12345678AB"))

  ".nextPage" - {

    "in Normal Mode" - {

      "must go to CYA when a valid Country is given" in {
        val country = emptyAnswers.set(QROPSCountryPage, Country("GB", "United Kingdom")).success.value

        QROPSCountryPage.nextPage(NormalMode, country) mustEqual routes.QROPSDetailsCYAController.onPageLoad()
      }

      "must go to Qrops Other Established Country Page when 'Other' is supplied" in {
        val otherCountry = emptyAnswers.set(QROPSCountryPage, Country("ZZ", "Other")).success.value

        QROPSCountryPage.nextPage(NormalMode, otherCountry) mustEqual
          routes.QROPSOtherCountryController.onPageLoad(NormalMode)
      }

      "must go to /there-is-a-problem page when no country present in user answers" in {
        QROPSCountryPage.nextPage(NormalMode, emptyAnswers) mustEqual controllers.routes.JourneyRecoveryController.onPageLoad()
      }
    }

    "in Check Mode" - {

      "must go to Check Answers when a valid country i s given" in {
        val country = emptyAnswers.set(QROPSCountryPage, Country("GB", "United Kingdom")).success.value

        QROPSCountryPage.nextPage(CheckMode, country) mustEqual routes.QROPSDetailsCYAController.onPageLoad()
      }

      "must go to Qrops Other Established Country Page when 'Other' is supplied" in {
        val otherCountry = emptyAnswers.set(QROPSCountryPage, Country("ZZ", "Other")).success.value

        QROPSCountryPage.nextPage(CheckMode, otherCountry) mustEqual
          routes.QROPSOtherCountryController.onPageLoad(NormalMode)
      }

      "must go to /there-is-a-problem page when no country present in user answers" in {
        QROPSCountryPage.nextPage(CheckMode, emptyAnswers) mustEqual controllers.routes.JourneyRecoveryController.onPageLoad()
      }
    }

    "in FinalCheckMode" - {
      "must got to Final Check Answers page" in {
        QROPSCountryPage.nextPage(FinalCheckMode, emptyAnswers) mustEqual
          controllers.checkYourAnswers.routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }

  "cleanup" - {
    "must remove qropsOtherCountry when Country is not Other" in {
      val otherCountry = emptyAnswers.set(QROPSOtherCountryPage, "OtherCountryLand").success.value

      QROPSCountryPage.cleanup(Some(Country("DE", "Germany")), otherCountry).success.value mustEqual
        UserAnswers(userAnswersTransferNumber, PstrNumber("12345678AB"), Json.obj("qropsDetails" -> Json.obj()), emptyAnswers.lastUpdated)
    }

    "must not remove values when value is Other" in {
      val otherCountry = emptyAnswers.set(QROPSOtherCountryPage, "OtherCountryLand").success.value

      QROPSCountryPage.cleanup(Some(Country("ZZ", "Other")), otherCountry).success.value mustEqual
        otherCountry
    }
  }
}

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

package pages.memberDetails

import controllers.memberDetails.routes
import models.{CheckMode, FinalCheckMode, NormalMode, PstrNumber, UserAnswers}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class MemberNinoPageSpec extends AnyFreeSpec with Matchers {

  private val emptyAnswers = UserAnswers("id", PstrNumber("12345678AB"))

  ".nextPage" - {

    val emptyAnswers = UserAnswers("id", PstrNumber("12345678AB"))

    "in Normal Mode" - {

      "must go to Member Date of Birth" in {

        MemberNinoPage.nextPage(NormalMode, emptyAnswers) mustEqual routes.MemberDateOfBirthController.onPageLoad(NormalMode)
      }
    }

    "in Check Mode" - {

      "must go to Check Answers" in {

        MemberNinoPage.nextPage(CheckMode, emptyAnswers) mustEqual routes.MemberDetailsCYAController.onPageLoad()
      }
    }

    "in FinalCheckMode" - {
      "must go to Final Check Answers page" in {
        MemberNinoPage.nextPage(FinalCheckMode, emptyAnswers) mustEqual
          controllers.checkYourAnswers.routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }

  "cleanup" - {

    "must remove MemberDoesNotHaveNinoPage when a NINO is supplied" in {
      val noNinoUA = emptyAnswers
        .set(MemberDoesNotHaveNinoPage, "no nino")
        .success
        .value

      val cleaned = MemberNinoPage.cleanup(Some("AA123456A"), noNinoUA).success.value

      cleaned.get(MemberDoesNotHaveNinoPage) mustBe None
    }

    "must not remove MemberDoesNotHaveNinoPage when NINO is not supplied" in {
      val noNinoUA = emptyAnswers
        .set(MemberDoesNotHaveNinoPage, "no nino")
        .success
        .value

      val cleaned = MemberNinoPage.cleanup(None, noNinoUA).success.value

      cleaned.get(MemberDoesNotHaveNinoPage) mustBe Some("no nino")
    }
  }
}

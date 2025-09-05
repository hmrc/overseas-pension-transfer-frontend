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

import base.SpecBase
import controllers.memberDetails.routes
import models.{CheckMode, FinalCheckMode, NormalMode}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class MemberIsResidentUKPageSpec extends AnyFreeSpec with SpecBase with Matchers {

  ".nextPage" - {

    "in Normal Mode" - {

      "must go to Check Answers page when 'true'" in {

        MemberIsResidentUKPage.nextPage(
          NormalMode,
          emptyUserAnswers.set(MemberIsResidentUKPage, true).success.value
        ) mustEqual routes.MemberDetailsCYAController.onPageLoad()
      }

      "must go to Member Has Ever Been Uk Resident when 'false'" in {

        MemberIsResidentUKPage.nextPage(
          NormalMode,
          emptyUserAnswers.set(MemberIsResidentUKPage, false).success.value
        ) mustEqual routes.MemberHasEverBeenResidentUKController.onPageLoad(NormalMode)
      }
    }

    "in Check Mode" - {

      "must go to Check Answers page when 'true'" in {

        MemberIsResidentUKPage.nextPage(
          CheckMode,
          emptyUserAnswers.set(MemberIsResidentUKPage, true).success.value
        ) mustEqual routes.MemberDetailsCYAController.onPageLoad()
      }

      "must go to Member Has Ever Been Uk Resident in check mode when 'false'" in {

        MemberIsResidentUKPage.nextPage(
          CheckMode,
          emptyUserAnswers.set(MemberIsResidentUKPage, false).success.value
        ) mustEqual routes.MemberHasEverBeenResidentUKController.onPageLoad(CheckMode)
      }
    }

    "in FinalCheckMode" - {
      "must go to Final Check Answers page" in {
        MemberIsResidentUKPage.nextPage(FinalCheckMode, emptyUserAnswers) mustEqual
          controllers.checkYourAnswers.routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}

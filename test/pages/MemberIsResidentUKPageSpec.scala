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

package pages

import base.SpecBase
import controllers.routes
import models.{CheckMode, NormalMode, UserAnswers}
import org.scalatest.matchers.must.Matchers

class MemberIsResidentUKPageSpec extends SpecBase with Matchers {

  ".nextPage" - {

    "in Normal Mode" - {

      "must go to Check Answers page when 'true'" in {

        MemberIsResidentUKPage.nextPage(
          NormalMode,
          emptyUserAnswers.set(MemberIsResidentUKPage, true).success.value
        ) mustEqual routes.CheckYourAnswersController.onPageLoad()
      }
    }

    "in Normal Mode" - {

      "must go to Next page when 'false'" in {

        MemberIsResidentUKPage.nextPage(
          NormalMode,
          emptyUserAnswers.set(MemberIsResidentUKPage, false).success.value
        ) mustEqual routes.MemberHasEverBeenResidentUKController.onPageLoad(NormalMode)
      }
    }

    "in Check Mode" - {

      "must go to Check Answers" in {

        MemberIsResidentUKPage.nextPage(CheckMode, emptyUserAnswers) mustEqual routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}

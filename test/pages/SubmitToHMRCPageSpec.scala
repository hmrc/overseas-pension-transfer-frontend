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
import models.{AmendCheckMode, NormalMode, PstrNumber, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec

class SubmitToHMRCPageSpec extends AnyFreeSpec with SpecBase {

  ".nextPage" - {

    val emptyAnswers = UserAnswers(userAnswersTransferNumber, PstrNumber("12345678AB"))

    "in Normal Mode" - {

      "must go to psa declaration if yes selected and user is psa" in {
        val ua = emptyAnswers.set(SubmitToHMRCPage, true).success.value
        SubmitToHMRCPage.nextPageWith(NormalMode, ua, psaUser) mustEqual routes.PsaDeclarationController.onPageLoad(NormalMode)
      }

      "must go to psp declaration if yes selected and user is psp" in {
        val ua = emptyAnswers.set(SubmitToHMRCPage, true).success.value
        SubmitToHMRCPage.nextPageWith(NormalMode, ua, pspUser) mustEqual routes.PspDeclarationController.onPageLoad(NormalMode)
      }

      "must go to dashboard if no selected" in {
        val ua = emptyAnswers.set(SubmitToHMRCPage, false).success.value
        SubmitToHMRCPage.nextPageWith(NormalMode, ua, pspUser) mustEqual controllers.routes.DashboardController.onPageLoad()
      }
    }

    "in AmendCheck Mode" - {

      "must go to psa declaration if yes selected and user is psa" in {
        val ua = emptyAnswers.set(SubmitToHMRCPage, true).success.value
        SubmitToHMRCPage.nextPageWith(AmendCheckMode, ua, psaUser) mustEqual routes.PsaDeclarationController.onPageLoad(AmendCheckMode)
      }

      "must go to psp declaration if yes selected and user is psp" in {
        val ua = emptyAnswers.set(SubmitToHMRCPage, true).success.value
        SubmitToHMRCPage.nextPageWith(AmendCheckMode, ua, pspUser) mustEqual routes.PspDeclarationController.onPageLoad(AmendCheckMode)
      }

      "must go to dashboard if no selected" in {
        val ua = emptyAnswers.set(SubmitToHMRCPage, false).success.value
        SubmitToHMRCPage.nextPageWith(AmendCheckMode, ua, pspUser) mustEqual controllers.routes.DashboardController.onPageLoad()
      }
    }
  }
}

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
import models.{NormalMode, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class SubmitToHMRCPageSpec extends AnyFreeSpec with SpecBase {

  ".nextPage" - {

    val emptyAnswers = UserAnswers("id")

    "in Normal Mode" - {

      "must go to psa declaration if yes selected and user is psa" in {
        val ua = emptyAnswers.set(SubmitToHMRCPage, true).success.value
        SubmitToHMRCPage.nextPageWith(NormalMode, ua, psaUser) mustEqual routes.PsaDeclarationController.onPageLoad()
      }

      "must go to psp declaration if yes selected and user is psp" in {
        val ua = emptyAnswers.set(SubmitToHMRCPage, true).success.value
        SubmitToHMRCPage.nextPageWith(NormalMode, ua, pspUser) mustEqual routes.PspDeclarationController.onPageLoad()
      }

      "must go to task list  if no selected" in {
        val ua = emptyAnswers.set(SubmitToHMRCPage, false).success.value
        SubmitToHMRCPage.nextPageWith(NormalMode, ua, pspUser) mustEqual controllers.routes.DashboardController.onPageLoad()
      }
    }
  }
}

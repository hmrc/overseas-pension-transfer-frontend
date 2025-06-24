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
import models.{CheckMode, NormalMode, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class AdditionalUnquotedSharePageSpec extends AnyFreeSpec with SpecBase with Matchers {

  ".nextPage" - {

    val emptyAnswers = UserAnswers("id")

    "in Normal Mode" - {

      "must go to Unquoted share company name page when 'true'" in {
        AdditionalUnquotedSharePage.nextPage(
          NormalMode,
          emptyAnswers.set(AdditionalUnquotedSharePage, true).success.value
        ) mustEqual routes.UnquotedShareCompanyNameController.onPageLoad(NormalMode)
      }
      "must go to TransferDetails CYA page when 'false'" in {
        AdditionalUnquotedSharePage.nextPage(
          NormalMode,
          emptyAnswers.set(AdditionalUnquotedSharePage, false).success.value
        ) mustEqual routes.TransferDetailsCYAController.onPageLoad()
      }
    }

    "in Check Mode" - {

      "must go to Check Answers" in {

        AdditionalUnquotedSharePage.nextPage(CheckMode, emptyAnswers) mustEqual routes.TransferDetailsCYAController.onPageLoad()
      }
    }
  }
}

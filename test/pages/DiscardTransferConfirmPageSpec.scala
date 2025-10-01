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

import controllers.routes
import models.{NormalMode, PstrNumber, UserAnswers}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

import scala.util.Try

class DiscardTransferConfirmPageSpec extends AnyFreeSpec with Matchers {

  ".nextPage" - {

    "in Normal Mode" - {

      "must go to Index when DiscardTransferConfirm in UserAnswers is true" in {
        val userAnswers: UserAnswers = UserAnswers("id", PstrNumber("12345678AB")).set(DiscardTransferConfirmPage, true).success.value

        DiscardTransferConfirmPage.nextPage(NormalMode, userAnswers) mustEqual routes.IndexController.onPageLoad()
      }

      "must go to Task List page when DiscardTransferConfirm in UserAnswers is false" in {
        val userAnswers: UserAnswers = UserAnswers("id", PstrNumber("12345678AB")).set(DiscardTransferConfirmPage, false).success.value

        DiscardTransferConfirmPage.nextPage(NormalMode, userAnswers) mustEqual routes.TaskListController.onPageLoad()
      }

      "must got to Journey Recovery page when User Answers is empty" in {
        val userAnswers: UserAnswers = UserAnswers("id", PstrNumber("12345678AB"))

        DiscardTransferConfirmPage.nextPage(NormalMode, userAnswers) mustEqual routes.JourneyRecoveryController.onPageLoad()
      }
    }
  }
}

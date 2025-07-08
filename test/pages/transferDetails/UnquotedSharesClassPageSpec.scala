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

import controllers.transferDetails.routes
import models.{CheckMode, NormalMode, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class UnquotedSharesClassPageSpec extends AnyFreeSpec with Matchers {
  private val index = 0

  ".nextPage" - {

    val emptyAnswers = UserAnswers("id")

    "in Normal Mode" - {

      "must go to Index" in {

        UnquotedSharesClassPage(index).nextPage(NormalMode, emptyAnswers) mustEqual routes.UnquotedShareCYAController.onPageLoad(index)
      }
    }

    "in Check Mode" - {

      "must go to Check Answers" in {

        UnquotedSharesClassPage(index).nextPage(CheckMode, emptyAnswers) mustEqual routes.UnquotedShareCYAController.onPageLoad(index)
      }
    }
  }
}

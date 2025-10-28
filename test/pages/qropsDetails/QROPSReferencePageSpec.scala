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
import models.{AmendCheckMode, CheckMode, FinalCheckMode, NormalMode, PstrNumber, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class QROPSReferencePageSpec extends AnyFreeSpec with Matchers with SpecBase {

  ".nextPage" - {

    val emptyAnswers = UserAnswers(userAnswersTransferNumber, PstrNumber("12345678AB"))

    "in Normal Mode" - {

      "must go to Index" in {

        QROPSReferencePage.nextPage(NormalMode, emptyAnswers) mustEqual routes.QROPSAddressController.onPageLoad(NormalMode)
      }
    }

    "in Check Mode" - {

      "must go to Check Answers" in {

        QROPSReferencePage.nextPage(CheckMode, emptyAnswers) mustEqual routes.QROPSDetailsCYAController.onPageLoad()
      }
    }

    "in FinalCheckMode" - {
      "must got to Final Check Ansers page" in {
        QROPSReferencePage.nextPage(FinalCheckMode, emptyAnswers) mustEqual
          controllers.checkYourAnswers.routes.CheckYourAnswersController.onPageLoad()
      }
    }

    "in AmendCheckMode" - {
      "must go to Amend Check Answers" in {
        QROPSReferencePage.nextPage(AmendCheckMode, emptyAnswers) mustEqual
          controllers.viewandamend.routes.ViewAmendSubmittedController.amend()
      }
    }
  }
}

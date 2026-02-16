/*
 * Copyright 2026 HM Revenue & Customs
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

package pages.viewandamend

import base.SpecBase
import models.QtStatus.AmendInProgress
import models.{NormalMode, PstrNumber, QtStatus, TransferId}
import org.scalatest.wordspec.AnyWordSpec

import java.util.UUID

class ViewAmendSubmittedPageSpec extends AnyWordSpec with SpecBase {

  val transferId: TransferId = TransferId(UUID.randomUUID().toString)
  val pstrNumber: PstrNumber = PstrNumber("QT123456")
  val qtStatus: QtStatus     = AmendInProgress
  val versionNumber: String  = "1"

  ".nextPage" when {

    "in normal mode" should {

      "redirect to the view and amend page" in {
        ViewAmendSubmittedPage.nextPage(NormalMode, userAnswersMemberName).url mustBe controllers.viewandamend.routes.ViewAmendSubmittedController.amend().url
      }
    }
  }

  ".userAnswersError" should {

    "redirect the user to SubmittedTransferSummaryController.onPageLoad" in {
      ViewAmendSubmittedPage.userAnswersError(
        transferId,
        pstrNumber,
        qtStatus,
        versionNumber
      ).url mustBe controllers.viewandamend.routes.SubmittedTransferSummaryController.onPageLoad(
        transferId,
        pstrNumber,
        qtStatus,
        versionNumber
      ).url
    }
  }

}

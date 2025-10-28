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

package viewmodels

import base.SpecBase
import models.NormalMode
import models.taskList.TaskStatus.{CannotStart, Completed, InProgress, NotStarted}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json
import viewmodels.TaskJourneyViewModels.{MemberDetailsJourneyViewModel, TransferDetailsJourneyViewModel}

class TaskJourneyViewModelSpec extends AnyFreeSpec with Matchers with SpecBase {

  private val validMemberDetails = Json.obj("memberDetails" -> Json.obj(
    "name"                   -> Json.obj("firstName" -> "Firstname", "lastName" -> "Lastname"),
    "nino"                   -> "AA000000A",
    "dateOfBirth"            -> "1993-11-11",
    "principalResAddDetails" -> Json.obj(
      "addressLine1" -> "line1",
      "addressLine2" -> "line2",
      "country"      -> Json.obj("code" -> "GB", "name" -> "United Kingdom")
    ),
    "memUkResident"          -> true
  ))

  private val validTransferDetails = Json.obj(
    "allowanceBeforeTransfer" -> 1000.25,
    "transferAmount"          -> 2000.88,
    "isTransferTaxable"       -> true,
    "paymentTaxableOverseas"  -> true,
    "whyTaxable"              -> "transferExceedsOTCAllowance",
    "whyTaxableOT"            -> "transferExceedsOTCAllowance",
    "applicableExclusion"     -> Set("occupational"),
    "amountTaxDeducted"       -> 100.33,
    "transferMinusTax"        -> 1900.99,
    "dateMemberTransferred"   -> "2025-04-01",
    "cashOnlyTransfer"        -> true,
    "typeOfAsset"             -> Seq("cashAssets"),
    "cashValue"               -> 2000.88
  )

  "MemberDetailsJourneyViewModel" - {
    "status" - {
      "must return Completed TaskStatus when Valid MemberDetails returned from Validator" in {

        MemberDetailsJourneyViewModel.status(emptyUserAnswers.copy(data = validMemberDetails)) mustBe
          Completed
      }

      "must return NotStarted when an empty UserAnswers" in {
        MemberDetailsJourneyViewModel.status(emptyUserAnswers) mustBe
          NotStarted
      }

      "must return InProgress when other Invalid Chain is returned" in {
        val missingMemberDetailsJson = Json.obj("memberDetails" -> Json.obj(
          "name" -> Json.obj("firstName" -> "Firstname", "lastName" -> "Lastname")
        ))

        MemberDetailsJourneyViewModel.status(emptyUserAnswers.copy(data = missingMemberDetailsJson)) mustBe
          InProgress
      }
    }

    "entry" - {
      "route to CYA when status is Completed" in {

        MemberDetailsJourneyViewModel.entry(emptyUserAnswers.copy(data = validMemberDetails)) mustBe
          controllers.memberDetails.routes.MemberDetailsCYAController.onPageLoad()
      }

      "route to member name page when status is not started" in {
        val validMemberDetailsJson = Json.obj("memberDetails" -> Json.obj())

        MemberDetailsJourneyViewModel.entry(emptyUserAnswers.copy(data = validMemberDetailsJson)) mustBe
          controllers.memberDetails.routes.MemberNameController.onPageLoad(NormalMode)
      }
    }

    "TransferDetailsJourneyViewModel" - {
      "status" - {
        "must return Completed when valid TransferDetails is present" in {

          val userAnswers = emptyUserAnswers.copy(
            data = validMemberDetails ++ Json.obj("transferDetails" -> validTransferDetails)
          )

          TransferDetailsJourneyViewModel.status(userAnswers) mustBe Completed
        }

        "must return CannotStart when MemberDetails is not completed" in {
          TransferDetailsJourneyViewModel.status(emptyUserAnswers) mustBe CannotStart
        }

        "must return NotStarted when MemberDetails is completed but TransferDetails is empty" in {

          val userAnswers = emptyUserAnswers.copy(data = validMemberDetails)

          TransferDetailsJourneyViewModel.status(userAnswers) mustBe NotStarted
        }

        "must return InProgress when partial TransferDetails are present" in {

          val partialTransferDetails = Json.obj(
            "allowanceBeforeTransfer" -> 1000.25,
            "transferAmount"          -> 2000.88,
            "isTransferTaxable"       -> true
          )

          val userAnswers = emptyUserAnswers.copy(
            data = validMemberDetails ++ Json.obj("transferDetails" -> partialTransferDetails)
          )

          TransferDetailsJourneyViewModel.status(userAnswers) mustBe InProgress
        }
      }

      "entry" - {
        "must route to TransferDetails CYA when status is Completed" in {

          val userAnswers = emptyUserAnswers.copy(
            data = validMemberDetails ++ Json.obj("transferDetails" -> validTransferDetails)
          )

          TransferDetailsJourneyViewModel.entry(userAnswers) mustBe
            controllers.transferDetails.routes.TransferDetailsCYAController.onPageLoad()
        }

        "must route to OverseasTransferAllowance page when status is NotStarted" in {

          val userAnswers = emptyUserAnswers.copy(data = validMemberDetails)

          TransferDetailsJourneyViewModel.entry(userAnswers) mustBe
            controllers.transferDetails.routes.OverseasTransferAllowanceController.onPageLoad(NormalMode)
        }
      }
    }
  }
}

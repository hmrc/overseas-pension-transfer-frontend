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
import controllers.checkYourAnswers.routes.CheckYourAnswersController
import models.taskList.TaskStatus.{Completed, InProgress, NotStarted}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json
import viewmodels.TaskJourneyViewModels.{MemberDetailsJourneyViewModel, TransferDetailsJourneyViewModel}

import java.time.LocalDate

class TaskJourneyViewModelSpec extends AnyFreeSpec with Matchers with SpecBase {

  "MemberDetailsJourneyViewModel" - {
    "status" - {
      "must return Completed TaskStatus when Valid MemberDetails returned from Validator" in {
        val validMemberDetailsJson = Json.obj("memberDetails" -> Json.obj(
          "name"                   -> Json.obj("firstName" -> "Firstname", "lastName" -> "Lastname"),
          "nino"                   -> "AA000000A",
          "dateOfBirth"            -> LocalDate.of(1993, 11, 11),
          "principalResAddDetails" -> Json.obj(
            "addressLine1" -> "line1",
            "addressLine2" -> "line2",
            "country"      -> Json.obj("code" -> "GB", "name" -> "United Kingdom")
          ),
          "memUkResident"          -> true
        ))

        MemberDetailsJourneyViewModel.status(emptyUserAnswers.copy(data = validMemberDetailsJson)) mustBe
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
        val validMemberDetailsJson = Json.obj("memberDetails" -> Json.obj(
          "name"                   -> Json.obj("firstName" -> "Firstname", "lastName" -> "Lastname"),
          "nino"                   -> "AA000000A",
          "dateOfBirth"            -> LocalDate.of(1993, 11, 11),
          "principalResAddDetails" -> Json.obj(
            "addressLine1" -> "line1",
            "addressLine2" -> "line2",
            "country"      -> Json.obj("code" -> "GB", "name" -> "United Kingdom")
          ),
          "memUkResident"          -> true
        ))

        MemberDetailsJourneyViewModel.entry(emptyUserAnswers.copy(data = validMemberDetailsJson)) mustBe
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
        "must return Completed TaskStatus when Valid TransferDetails returned from Validator" in {
          val validMemberDetails = Json.obj(
            "memberDetails" -> Json.obj(
              "name"                   -> Json.obj("firstName" -> "Firstname", "lastName" -> "Lastname"),
              "nino"                   -> "AA000000A",
              "dateOfBirth"            -> "1993-11-11",
              "principalResAddDetails" -> Json.obj(
                "addressLine1" -> "line1",
                "addressLine2" -> "line2",
                "country"      -> Json.obj("code" -> "GB", "name" -> "United Kingdom")
              ),
              "memUkResident"          -> true
            )
          )

          val validTransferDetails = Json.obj(
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

          val userAnswers = emptyUserAnswers.copy(data = validMemberDetails ++ Json.obj("transferDetails" -> validTransferDetails))
          MemberDetailsJourneyViewModel.status(userAnswers) mustBe Completed
          TransferDetailsJourneyViewModel.status(userAnswers) mustBe Completed
        }
      }

      "must return NotStarted when an empty UserAnswers" in {
        TransferDetailsJourneyViewModel.status(emptyUserAnswers) mustBe NotStarted //failing
      }

      "must return InProgress when partial data is present" in {
        val validMemberDetailsJson = Json.obj("memberDetails" -> Json.obj(
          "name"                   -> Json.obj("firstName" -> "Firstname", "lastName" -> "Lastname"),
          "nino"                   -> "AA000000A",
          "dateOfBirth"            -> LocalDate.of(1993, 11, 11),
          "principalResAddDetails" -> Json.obj(
            "addressLine1" -> "line1",
            "addressLine2" -> "line2",
            "country"      -> Json.obj("code" -> "GB", "name" -> "United Kingdom")
          ),
          "memUkResident"          -> true
        ))

        val partialTransferDetailsJson = validMemberDetailsJson ++ Json.obj("transferDetails" -> Json.obj(
          "allowanceBeforeTransfer" -> 1000.25,
          "transferAmount"          -> 2000.88,
          "isTransferTaxable"       -> true
        ))

        TransferDetailsJourneyViewModel.status(emptyUserAnswers.copy(data = partialTransferDetailsJson)) mustBe InProgress
      }
    }

    "entry" - {
      "must route to CYA when status is Completed" in {
        val validMemberDetailsJson = Json.obj("memberDetails" -> Json.obj(
          "name"                   -> Json.obj("firstName" -> "Firstname", "lastName" -> "Lastname"),
          "nino"                   -> "JZ667788C",
          "dateOfBirth"            -> LocalDate.of(1993, 11, 11),
          "principalResAddDetails" -> Json.obj(
            "addressLine1" -> "line1",
            "addressLine2" -> "line2",
            "country"      -> Json.obj("code" -> "GB", "name" -> "United Kingdom")
          ),
          "memUkResident"          -> true
        ))

        val result = MemberDetailsJourneyViewModel.entry(emptyUserAnswers.copy(data = validMemberDetailsJson))
        result mustBe CheckYourAnswersController.onPageLoad()
      }
    }
  }
}

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
import models.address.{Country, QROPSAddress, SchemeManagersAddress}
import models.taskList.TaskStatus.{CannotStart, Completed, InProgress, NotStarted}
import models.{NormalMode, PersonName, SchemeManagerType}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.qropsDetails.{QROPSAddressPage, QROPSCountryPage, QROPSNamePage, QROPSReferencePage}
import pages.qropsSchemeManagerDetails._
import play.api.libs.json.Json
import viewmodels.TaskJourneyViewModels._

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

    "QropsDetailsJourneyViewModel" - {
      "status" - {
        "must return Completed when valid QROPS details are present" in {
          val baseAnswers = emptyUserAnswers.copy(
            data = validMemberDetails ++ Json.obj("transferDetails" -> validTransferDetails)
          )

          val userAnswers = baseAnswers
            .set(QROPSNamePage, "Test QROPS").success.value
            .set(QROPSReferencePage, "Q123456").success.value
            .set(
              QROPSAddressPage,
              QROPSAddress(
                addressLine1 = "line1",
                addressLine2 = "line2",
                addressLine3 = None,
                addressLine4 = None,
                addressLine5 = None,
                country      = Country("GB", "United Kingdom")
              )
            ).success.value
            .set(QROPSCountryPage, Country("GB", "United Kingdom")).success.value

          QropsDetailsJourneyViewModel.status(userAnswers) mustBe Completed
        }

        "must return CannotStart when MemberDetails are not completed" in {
          QropsDetailsJourneyViewModel.status(emptyUserAnswers) mustBe CannotStart
        }

        "must return NotStarted when MemberDetails is completed but QROPS details are empty" in {
          val userAnswers = emptyUserAnswers.copy(data = validMemberDetails)
          QropsDetailsJourneyViewModel.status(userAnswers) mustBe NotStarted
        }

        "must return InProgress when partial QROPS details are present and MemberDetails is completed" in {
          val userAnswers = emptyUserAnswers
            .copy(data = validMemberDetails)
            .set(QROPSNamePage, "Test QROPS").success.value
            .set(QROPSReferencePage, "Q123456").success.value

          QropsDetailsJourneyViewModel.status(userAnswers) mustBe InProgress
        }

        "entry" - {
          "must route to QROPS CYA when status is Completed" in {
            val userAnswers = emptyUserAnswers
              .copy(data = validMemberDetails ++ Json.obj("transferDetails" -> validTransferDetails))
              .set(QROPSNamePage, "Test QROPS").success.value
              .set(QROPSReferencePage, "Q123456").success.value
              .set(
                QROPSAddressPage,
                QROPSAddress(
                  addressLine1 = "line1",
                  addressLine2 = "line2",
                  addressLine3 = None,
                  addressLine4 = None,
                  addressLine5 = None,
                  country      = Country("GB", "United Kingdom")
                )
              ).success.value
              .set(QROPSCountryPage, Country("GB", "United Kingdom")).success.value

            QropsDetailsJourneyViewModel.status(userAnswers) mustBe Completed
            QropsDetailsJourneyViewModel.entry(userAnswers) mustBe
              controllers.qropsDetails.routes.QROPSDetailsCYAController.onPageLoad()
          }

          "must route to QROPS name page when status is NotStarted" in {
            val userAnswers = emptyUserAnswers.copy(
              data = validMemberDetails ++ Json.obj("transferDetails" -> validTransferDetails)
            )
            QropsDetailsJourneyViewModel.entry(userAnswers) mustBe
              controllers.qropsDetails.routes.QROPSNameController.onPageLoad(NormalMode)
          }
        }
      }
    }

    "SchemeManagerDetailsJourneyViewModel" - {
      "status" - {
        "must return Completed when valid Scheme Manager details are present (Individual)" in {
          val baseAnswers = emptyUserAnswers.copy(
            data = validMemberDetails ++ Json.obj("transferDetails" -> validTransferDetails)
          )

          val userAnswers = baseAnswers
            .set(SchemeManagerTypePage, SchemeManagerType.Individual).success.value
            .set(SchemeManagersNamePage, PersonName("John", "Doe")).success.value
            .set(
              SchemeManagersAddressPage,
              SchemeManagersAddress(
                addressLine1 = "line1",
                addressLine2 = "line2",
                addressLine3 = None,
                addressLine4 = None,
                addressLine5 = None,
                country      = Country("GB", "United Kingdom")
              )
            ).success.value
            .set(SchemeManagersEmailPage, "test@gmail.co.uk").success.value
            .set(SchemeManagersContactPage, "0121456789").success.value

          SchemeManagerDetailsJourneyViewModel.status(userAnswers) mustBe Completed
        }

        "must return Completed when valid Scheme Manager details are present (Organisation)" in {
          val baseAnswers = emptyUserAnswers.copy(
            data = validMemberDetails ++ Json.obj("transferDetails" -> validTransferDetails)
          )

          val userAnswers = baseAnswers
            .set(SchemeManagerTypePage, SchemeManagerType.Organisation).success.value
            .set(SchemeManagerOrganisationNamePage, "Test Org").success.value
            .set(SchemeManagerOrgIndividualNamePage, PersonName("Contact", "Person")).success.value
            .set(
              SchemeManagersAddressPage,
              SchemeManagersAddress(
                addressLine1 = "line1",
                addressLine2 = "line2",
                addressLine3 = None,
                addressLine4 = None,
                addressLine5 = None,
                country      = Country("GB", "United Kingdom")
              )
            ).success.value
            .set(SchemeManagersEmailPage, "org@gmail.co.uk").success.value
            .set(SchemeManagersContactPage, "0787654321").success.value

          SchemeManagerDetailsJourneyViewModel.status(userAnswers) mustBe Completed
        }

        "must return CannotStart when MemberDetails is not completed" in {
          SchemeManagerDetailsJourneyViewModel.status(emptyUserAnswers) mustBe CannotStart
        }

        "must return NotStarted when required previous sections are completed but no Scheme Manager details" in {
          val userAnswers = emptyUserAnswers.copy(
            data = validMemberDetails ++ Json.obj("transferDetails" -> validTransferDetails)
          )
          SchemeManagerDetailsJourneyViewModel.status(userAnswers) mustBe NotStarted
        }

        "must return InProgress when partial Scheme Manager details are present (Individual)" in {
          val baseAnswers = emptyUserAnswers.copy(
            data = validMemberDetails ++ Json.obj("transferDetails" -> validTransferDetails)
          )

          val userAnswers = baseAnswers
            .set(SchemeManagerTypePage, SchemeManagerType.Individual).success.value
            .set(SchemeManagersNamePage, PersonName("John", "Doe")).success.value
            .set(SchemeManagersEmailPage, "test@example.com").success.value

          SchemeManagerDetailsJourneyViewModel.status(userAnswers) mustBe InProgress
        }

        "must return InProgress when partial Scheme Manager details are present (Organisation)" in {
          val baseAnswers = emptyUserAnswers.copy(
            data = validMemberDetails ++ Json.obj("transferDetails" -> validTransferDetails)
          )

          val userAnswers = baseAnswers
            .set(SchemeManagerTypePage, SchemeManagerType.Organisation).success.value
            .set(SchemeManagerOrganisationNamePage, "Test Org").success.value
            .set(SchemeManagerOrgIndividualNamePage, PersonName("Contact", "Person")).success.value

          SchemeManagerDetailsJourneyViewModel.status(userAnswers) mustBe InProgress
        }
      }

      "entry" - {
        "must route to Scheme Manager CYA when status is Completed (Individual)" in {
          val userAnswers = emptyUserAnswers
            .copy(data = validMemberDetails ++ Json.obj("transferDetails" -> validTransferDetails))
            .set(SchemeManagerTypePage, SchemeManagerType.Individual).success.value
            .set(SchemeManagersNamePage, PersonName("John", "Doe")).success.value
            .set(
              SchemeManagersAddressPage,
              SchemeManagersAddress(
                addressLine1 = "line1",
                addressLine2 = "line2",
                addressLine3 = None,
                addressLine4 = None,
                addressLine5 = None,
                country      = Country("GB", "United Kingdom")
              )
            ).success.value
            .set(SchemeManagersEmailPage, "test@example.com").success.value
            .set(SchemeManagersContactPage, "0121456789").success.value

          SchemeManagerDetailsJourneyViewModel.status(userAnswers) mustBe Completed
          SchemeManagerDetailsJourneyViewModel.entry(userAnswers) mustBe
            controllers.qropsSchemeManagerDetails.routes.SchemeManagerDetailsCYAController.onPageLoad()
        }

        "must route to Scheme Manager CYA when status is Completed (Organisation)" in {
          val userAnswers = emptyUserAnswers
            .copy(data = validMemberDetails ++ Json.obj("transferDetails" -> validTransferDetails))
            .set(SchemeManagerTypePage, SchemeManagerType.Organisation).success.value
            .set(SchemeManagerOrganisationNamePage, "Test Org").success.value
            .set(SchemeManagerOrgIndividualNamePage, PersonName("Contact", "Person")).success.value
            .set(
              SchemeManagersAddressPage,
              SchemeManagersAddress(
                addressLine1 = "line1",
                addressLine2 = "line2",
                addressLine3 = None,
                addressLine4 = None,
                addressLine5 = None,
                country      = Country("GB", "United Kingdom")
              )
            ).success.value
            .set(SchemeManagersEmailPage, "org@gmail.co.uk").success.value
            .set(SchemeManagersContactPage, "0787654321").success.value

          SchemeManagerDetailsJourneyViewModel.status(userAnswers) mustBe Completed
          SchemeManagerDetailsJourneyViewModel.entry(userAnswers) mustBe
            controllers.qropsSchemeManagerDetails.routes.SchemeManagerDetailsCYAController.onPageLoad()
        }

        "must route to Scheme Manager Type page when status is NotStarted" in {
          val userAnswers = emptyUserAnswers.copy(
            data = validMemberDetails ++ Json.obj("transferDetails" -> validTransferDetails)
          )

          SchemeManagerDetailsJourneyViewModel.entry(userAnswers) mustBe
            controllers.qropsSchemeManagerDetails.routes.SchemeManagerTypeController.onPageLoad(NormalMode)
        }
      }
    }

    "SubmissionDetailsJourneyViewModel" - {
      "status" - {
        "must return NotStarted when all required sections are completed" in {
          val baseAnswers = emptyUserAnswers.copy(
            data = validMemberDetails ++ Json.obj("transferDetails" -> validTransferDetails)
          )

          val userAnswers = baseAnswers
            .set(QROPSNamePage, "Test QROPS").success.value
            .set(QROPSReferencePage, "Q123456").success.value
            .set(
              QROPSAddressPage,
              QROPSAddress(
                addressLine1 = "line1",
                addressLine2 = "line2",
                addressLine3 = None,
                addressLine4 = None,
                addressLine5 = None,
                country      = Country("GB", "United Kingdom")
              )
            ).success.value
            .set(QROPSCountryPage, Country("GB", "United Kingdom")).success.value
            .set(SchemeManagerTypePage, SchemeManagerType.Individual).success.value
            .set(SchemeManagersNamePage, PersonName("John", "Doe")).success.value
            .set(
              SchemeManagersAddressPage,
              SchemeManagersAddress(
                addressLine1 = "line1",
                addressLine2 = "line2",
                addressLine3 = None,
                addressLine4 = None,
                addressLine5 = None,
                country      = Country("GB", "United Kingdom")
              )
            ).success.value
            .set(SchemeManagersEmailPage, "test@gmail.co.uk").success.value
            .set(SchemeManagersContactPage, "0121456789").success.value

          SubmissionDetailsJourneyViewModel.status(userAnswers) mustBe NotStarted
        }

        "must return CannotStart when MemberDetails is not completed" in {
          val userAnswers = emptyUserAnswers
          SubmissionDetailsJourneyViewModel.status(userAnswers) mustBe CannotStart
        }

        "must return CannotStart when TransferDetails is not completed" in {
          val userAnswers = emptyUserAnswers.copy(data = validMemberDetails)
          SubmissionDetailsJourneyViewModel.status(userAnswers) mustBe CannotStart
        }

        "must return CannotStart when QropsDetails is not completed" in {
          val userAnswers = emptyUserAnswers.copy(
            data = validMemberDetails ++ Json.obj("transferDetails" -> validTransferDetails)
          )
          SubmissionDetailsJourneyViewModel.status(userAnswers) mustBe CannotStart
        }

        "must return CannotStart when SchemeManagerDetails is not completed" in {
          val baseAnswers = emptyUserAnswers.copy(
            data = validMemberDetails ++ Json.obj("transferDetails" -> validTransferDetails)
          )

          val userAnswers = baseAnswers
            .set(QROPSNamePage, "Test QROPS").success.value
            .set(QROPSReferencePage, "Q123456").success.value
            .set(
              QROPSAddressPage,
              QROPSAddress(
                addressLine1 = "line1",
                addressLine2 = "line2",
                addressLine3 = None,
                addressLine4 = None,
                addressLine5 = None,
                country      = Country("GB", "United Kingdom")
              )
            ).success.value
            .set(QROPSCountryPage, Country("GB", "United Kingdom")).success.value

          SubmissionDetailsJourneyViewModel.status(userAnswers) mustBe CannotStart
        }
        "entry" - {
          "must route to Check Your Answers page" in {
            SubmissionDetailsJourneyViewModel.entry(emptyUserAnswers) mustBe
              controllers.checkYourAnswers.routes.CheckYourAnswersController.onPageLoad()
          }
        }
      }
    }
  }
}

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

package validators

import base.SpecBase
import cats.data.Chain
import cats.data.Validated.{Invalid, Valid}
import models.address.{Country, MembersCurrentAddress, MembersLastUKAddress}
import models.transferJourneys.MemberDetails
import models.{DataMissingError, GenericError, PersonName}
import org.scalatest.freespec.AnyFreeSpec
import pages.memberDetails._
import play.api.libs.json.Json

import java.time.LocalDate

class MemberDetailsValidatorSpec extends AnyFreeSpec with SpecBase {

  private val memberDetails = MemberDetails(
    PersonName("Firstname", "Lastname"),
    None,
    None,
    LocalDate.of(1993, 11, 11),
    MembersCurrentAddress(
      "line1",
      "line2",
      None,
      None,
      Country("GB", "United Kingdom"),
      None,
      None
    ),
    isUkResident = true,
    None,
    None,
    None
  )

  "fromUserAnswers" - {
    "return valid MemberDetails" - {
      "isUkResident = true with memberNino" in {
        val validJson = Json.obj("memberDetails" -> Json.obj(
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

        MemberDetailsValidator.fromUserAnswers(emptyUserAnswers.copy(data = validJson)) mustBe
          Valid(memberDetails.copy(memberNino = Some("AA000000A")))
      }

      "isUkResident = true with reasonNoNino" in {
        val validJson = Json.obj("memberDetails" -> Json.obj(
          "name"                   -> Json.obj("firstName" -> "Firstname", "lastName" -> "Lastname"),
          "reasonNoNINO"           -> "Forgot it",
          "dateOfBirth"            -> LocalDate.of(1993, 11, 11),
          "principalResAddDetails" -> Json.obj(
            "addressLine1" -> "line1",
            "addressLine2" -> "line2",
            "country"      -> Json.obj("code" -> "GB", "name" -> "United Kingdom")
          ),
          "memUkResident"          -> true
        ))

        MemberDetailsValidator.fromUserAnswers(emptyUserAnswers.copy(data = validJson)) mustBe
          Valid(memberDetails.copy(reasonNoNino = Some("Forgot it")))
      }

      "isUkResident = false with hasEverBeenUkResident = false" in {
        val validJson = Json.obj("memberDetails" -> Json.obj(
          "name"                   -> Json.obj("firstName" -> "Firstname", "lastName" -> "Lastname"),
          "nino"                   -> "AA000000A",
          "dateOfBirth"            -> LocalDate.of(1993, 11, 11),
          "principalResAddDetails" -> Json.obj(
            "addressLine1" -> "line1",
            "addressLine2" -> "line2",
            "country"      -> Json.obj("code" -> "GB", "name" -> "United Kingdom")
          ),
          "memUkResident"          -> false,
          "memEverUkResident"      -> false
        ))

        MemberDetailsValidator.fromUserAnswers(emptyUserAnswers.copy(data = validJson)) mustBe
          Valid(memberDetails.copy(memberNino = Some("AA000000A"), isUkResident = false, hasBeenUkResident = Some(false)))
      }

      "isUkResident = false with hasEverBeenUkResident = true" in {
        val validJson = Json.obj("memberDetails" -> Json.obj(
          "name"                    -> Json.obj("firstName" -> "Firstname", "lastName" -> "Lastname"),
          "nino"                    -> "AA000000A",
          "dateOfBirth"             -> LocalDate.of(1993, 11, 11),
          "principalResAddDetails"  -> Json.obj(
            "addressLine1" -> "line1",
            "addressLine2" -> "line2",
            "country"      -> Json.obj("code" -> "GB", "name" -> "United Kingdom")
          ),
          "memUkResident"           -> false,
          "memEverUkResident"       -> true,
          "lastPrincipalAddDetails" -> Json.obj(
            "addressLine1" -> "line1",
            "addressLine2" -> "line2",
            "ukPostCode"   -> "AA11 1AA"
          ),
          "dateMemberLeftUk"        -> LocalDate.of(2023, 6, 27)
        ))

        MemberDetailsValidator.fromUserAnswers(emptyUserAnswers.copy(data = validJson)) mustBe
          Valid(memberDetails.copy(
            memberNino           = Some("AA000000A"),
            isUkResident         = false,
            hasBeenUkResident    = Some(true),
            lastPrincipalAddress = Some(MembersLastUKAddress(
              "line1",
              "line2",
              None,
              None,
              "AA11 1AA"
            )),
            dateLeftUk           = Some(LocalDate.of(2023, 6, 27))
          ))
      }
    }

    "return Invalid NEC" - {
      "when there are no member details" in {
        val invalidJson = Json.obj("memberDetails" -> Json.obj())

        MemberDetailsValidator.fromUserAnswers(emptyUserAnswers.copy(data = invalidJson)) mustBe
          Invalid(
            Chain(
              DataMissingError(MemberNamePage),
              DataMissingError(MemberNinoPage),
              DataMissingError(MemberDoesNotHaveNinoPage),
              DataMissingError(MemberDateOfBirthPage),
              DataMissingError(MembersCurrentAddressPage),
              DataMissingError(MemberIsResidentUKPage),
              DataMissingError(MemberHasEverBeenResidentUKPage),
              DataMissingError(MembersLastUKAddressPage),
              DataMissingError(MemberDateOfLeavingUKPage)
            )
          )
      }

      "when memberNino and reasonNoNino are both present" in {
        val invalidJson = Json.obj("memberDetails" -> Json.obj(
          "name"                   -> Json.obj("firstName" -> "Firstname", "lastName" -> "Lastname"),
          "nino"                   -> "AA000000A",
          "reasonNoNINO"           -> "Forgot it",
          "dateOfBirth"            -> LocalDate.of(1993, 11, 11),
          "principalResAddDetails" -> Json.obj(
            "addressLine1" -> "line1",
            "addressLine2" -> "line2",
            "country"      -> Json.obj("code" -> "GB", "name" -> "United Kingdom")
          ),
          "memUkResident"          -> true
        ))

        MemberDetailsValidator.fromUserAnswers(emptyUserAnswers.copy(data = invalidJson)) mustBe
          Invalid(
            Chain(
              GenericError("Cannot have valid payload with nino and reasonNoNINO"),
              GenericError("Cannot have valid payload with nino and reasonNoNINO")
            )
          )
      }

      "isUkResident = false and rest of journey is none" in {
        val invalidJson = Json.obj("memberDetails" -> Json.obj(
          "name"                   -> Json.obj("firstName" -> "Firstname", "lastName" -> "Lastname"),
          "nino"                   -> "AA000000A",
          "dateOfBirth"            -> LocalDate.of(1993, 11, 11),
          "principalResAddDetails" -> Json.obj(
            "addressLine1" -> "line1",
            "addressLine2" -> "line2",
            "country"      -> Json.obj("code" -> "GB", "name" -> "United Kingdom")
          ),
          "memUkResident"          -> false
        ))

        MemberDetailsValidator.fromUserAnswers(emptyUserAnswers.copy(data = invalidJson)) mustBe
          Invalid(
            Chain(
              DataMissingError(MemberHasEverBeenResidentUKPage),
              DataMissingError(MembersLastUKAddressPage),
              DataMissingError(MemberDateOfLeavingUKPage)
            )
          )
      }

      "isUkResident = false, hasEverBeenUkResident = true and rest of journey is none" in {
        val invalidJson = Json.obj("memberDetails" -> Json.obj(
          "name"                   -> Json.obj("firstName" -> "Firstname", "lastName" -> "Lastname"),
          "nino"                   -> "AA000000A",
          "dateOfBirth"            -> LocalDate.of(1993, 11, 11),
          "principalResAddDetails" -> Json.obj(
            "addressLine1" -> "line1",
            "addressLine2" -> "line2",
            "country"      -> Json.obj("code" -> "GB", "name" -> "United Kingdom")
          ),
          "memUkResident"          -> false,
          "memEverUkResident"      -> true
        ))

        MemberDetailsValidator.fromUserAnswers(emptyUserAnswers.copy(data = invalidJson)) mustBe
          Invalid(
            Chain(
              DataMissingError(MembersLastUKAddressPage),
              DataMissingError(MemberDateOfLeavingUKPage)
            )
          )
      }
    }
  }
}

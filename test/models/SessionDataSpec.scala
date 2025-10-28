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

package models

import base.SpecBase
import models.TaskCategory._
import models.taskList.TaskStatus
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.SubmitToHMRCPage
import play.api.libs.json._
import queries.TaskStatusQuery
import services.EncryptionService

import java.time.Instant

class SessionDataSpec extends AnyFreeSpec with Matchers with SpecBase {

  private val encryptionService = new EncryptionService("F42sAkGScIpm4Vlui6XGpKW/zvmfyAYyoNHeLVQuoCk=")

  private val sessionData = SessionData(
    sessionId         = "Int-8d355b23-d997-4ea4-b766-c547334f313a",
    transferId        = TransferNumber("5772e197-70ff-4767-8409-44f18774eb75"),
    schemeInformation = schemeDetails,
    user              = psaUser.updatePensionSchemeDetails(schemeDetails),
    data              = Json.obj("memberDetails" -> Json.obj("status" -> "inProgress")),
    lastUpdated       = Instant.now()
  )

  "get" - {
    "should get Some value from data Json when value is present" in {
      emptySessionData.copy(data = Json.obj("submitToHMRC" -> true, "key" -> "value")).get(SubmitToHMRCPage) mustBe
        Some(true)
    }

    "should get None when no value present in data Json" in {
      emptySessionData.get(SubmitToHMRCPage) mustBe None
    }
  }

  "set" - {
    "should update Json in data field" in {
      emptySessionData.set(SubmitToHMRCPage, false).success.value.data mustBe
        Json.obj("submitToHMRC" -> false)
    }
  }

  "remove" - {
    "should remove existing Json from data field" in {
      emptySessionData.copy(data = Json.obj("submitToHMRC" -> true, "key" -> "value"))
        .remove(SubmitToHMRCPage).success.value.data mustBe
        Json.obj("key" -> "value")
    }
  }

  "initialise" - {
    "should set expected default statuses" in {
      val sd = SessionData.initialise(emptySessionData).get

      sd.transferId mustBe userAnswersTransferNumber
      sd.get(TaskStatusQuery(MemberDetails)) mustBe Some(TaskStatus.NotStarted)
      sd.get(TaskStatusQuery(QROPSDetails)) mustBe Some(TaskStatus.CannotStart)
      sd.get(TaskStatusQuery(SchemeManagerDetails)) mustBe Some(TaskStatus.CannotStart)
      sd.get(TaskStatusQuery(TransferDetails)) mustBe Some(TaskStatus.CannotStart)
      sd.get(TaskStatusQuery(SubmissionDetails)) mustBe Some(TaskStatus.CannotStart)
    }
  }

  "Encryption wrappers" - {

    "should encrypt and decrypt session data correctly" in {
      import SessionData._

      val decryptedWrapper = DecryptedSessionData(sessionData.data)
      val encryptedWrapper = decryptedWrapper.encrypt(encryptionService)

      encryptedWrapper.encryptedString must not be empty

      val decryptedBack = encryptedWrapper.decrypt(encryptionService)
      decryptedBack.isRight mustBe true
      decryptedBack.getOrElse(Json.obj()) mustEqual sessionData.data
    }

    "should produce different ciphertexts for the same input (due to random IV)" in {
      import SessionData._

      val enc1 = DecryptedSessionData(sessionData.data).encrypt(encryptionService)
      val enc2 = DecryptedSessionData(sessionData.data).encrypt(encryptionService)

      enc1.encryptedString must not equal enc2.encryptedString
    }

    "should return Left when decryption fails for invalid cipher text" in {
      import SessionData._
      val badEnc = EncryptedSessionData("invalid-cipher-text")

      val result = badEnc.decrypt(encryptionService)
      result.isLeft mustBe true
    }
  }

  "encryptedFormat" - {

    "should encrypt and decrypt using encryptedFormat successfully" in {
      val format = SessionData.encryptedFormat(encryptionService)

      val written = format.writes(sessionData)

      (written \ "data").as[String] must not be empty
      (written \ "data").as[String] must not include ("memberDetails")

      val readBack = format.reads(written).get

      readBack.sessionId mustBe sessionData.sessionId
      (readBack.data \ "memberDetails" \ "status").as[String] mustBe "inProgress"
    }

    "should throw RuntimeException if decryption fails due to invalid cipher" in {
      val invalidJson = Json.obj(
        "_id"               -> sessionData.sessionId,
        "transferId"        -> sessionData.transferId,
        "schemeInformation" -> Json.toJson(sessionData.schemeInformation),
        "user"              -> Json.toJson(sessionData.user),
        "data"              -> "invalid-encrypted-data",
        "lastUpdated"       -> Json.toJson(sessionData.lastUpdated)
      )

      val format = SessionData.encryptedFormat(encryptionService)

      intercept[RuntimeException] {
        format.reads(invalidJson).get
      }
    }
  }
}

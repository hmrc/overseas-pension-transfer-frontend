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
import org.scalatest.freespec.AnyFreeSpec
import play.api.libs.json._
import services.EncryptionService

class DashboardDataSpec extends AnyFreeSpec with SpecBase {

  val encryptionService = new EncryptionService("F42sAkGScIpm4Vlui6XGpKW/zvmfyAYyoNHeLVQuoCk=")

  val testData = DashboardData(
    id          = "Int-123",
    data        = Json.obj("transfers" -> Json.obj("qtStatus" -> "InProgress")),
    lastUpdated = now
  )

  "DashboardData.encryptedFormat" - {

    "encrypt and decrypt DashboardData successfully" in {
      val format = DashboardData.encryptedFormat(encryptionService)

      val encryptedJson = format.writes(testData)
      (encryptedJson \ "data").as[String] must not be empty

      val decrypted = format.reads(encryptedJson).get
      (decrypted.data \ "transfers" \ "qtStatus").as[String] mustEqual "InProgress"
      decrypted.id mustEqual testData.id
    }

    "fail to decrypt if ciphertext is invalid" in {
      val invalidJson = Json.obj(
        "_id"         -> "Int-123",
        "data"        -> "invalid-encrypted-data",
        "lastUpdated" -> now
      )

      val format = DashboardData.encryptedFormat(encryptionService)

      intercept[RuntimeException] {
        format.reads(invalidJson).get
      }
    }

    "correctly encrypt using wrapper classes" in {
      import DashboardData._

      val decryptedWrapper = DecryptedDashboardData(Json.obj("foo" -> "bar"))
      val encryptedWrapper = decryptedWrapper.encrypt(encryptionService)

      encryptedWrapper.encryptedString must not be JsObject.empty

      val decryptedBack = encryptedWrapper.decrypt(encryptionService)
      decryptedBack.isRight mustBe true
      decryptedBack.getOrElse(Json.obj()) mustEqual Json.obj("foo" -> "bar")
    }
  }
}

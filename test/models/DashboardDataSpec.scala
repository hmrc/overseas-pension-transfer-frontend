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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json._
import services.EncryptionService

import java.time.Instant

class DashboardDataSpec extends AnyWordSpec with Matchers {

  val encryptionService = new EncryptionService("F42sAkGScIpm4Vlui6XGpKW/zvmfyAYyoNHeLVQuoCk=")

  val testData = DashboardData(
    id          = "Int-123",
    data        = Json.obj("transfers" -> Json.obj("qtStatus" -> "InProgress")),
    lastUpdated = Instant.now()
  )

  "DashboardData.encryptedFormat" should {

    "encrypt and decrypt DashboardData successfully" in {
      val format = DashboardData.encryptedFormat(encryptionService)

      val encryptedJson = format.writes(testData)
      (encryptedJson \ "data").as[String] should not be empty

      val decrypted = format.reads(encryptedJson).get
      (decrypted.data \ "transfers" \ "qtStatus").as[String] shouldEqual "InProgress"
      decrypted.id shouldEqual testData.id
    }

    "fail to decrypt if ciphertext is invalid" in {
      val invalidJson = Json.obj(
        "_id"         -> "Int-123",
        "data"        -> "invalid-encrypted-data",
        "lastUpdated" -> Instant.now()
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

      encryptedWrapper.encryptedString should not be JsObject.empty

      val decryptedBack = encryptedWrapper.decrypt(encryptionService)
      decryptedBack.isRight shouldBe true
      decryptedBack.getOrElse(Json.obj()) shouldEqual Json.obj("foo" -> "bar")
    }
  }
}

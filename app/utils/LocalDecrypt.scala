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

package utils

import com.typesafe.config.{ConfigFactory, ConfigValueType}
import play.api.Logging
import play.api.libs.json._
import services.EncryptionService

import java.util.Base64

object LocalDecrypt extends App with Logging {

  if (args.length != 1) {
    logger.error(
      "Usage: sbt \"runMain utils.LocalDecrypt <base64Json>\""
    )
    sys.exit(1)
  }

  val decodedBytes   = Base64.getDecoder.decode(args(0))
  val fullJsonString = new String(decodedBytes, "UTF-8")

  val originalJson = Json.parse(fullJsonString).as[JsObject]

  val encrypted = (originalJson \ "data").asOpt[String].getOrElse {
    throw new IllegalArgumentException("Could not find 'data' field in JSON")
  }

  val config = ConfigFactory.load()

  def getOptionalString(path: String): Option[String] =
    if (config.hasPath(path) && config.getValue(path).valueType() == ConfigValueType.STRING)
      Some(config.getString(path))
    else None

  val masterKey = getOptionalString("encryption.masterKey")
    .orElse(getOptionalString("encryption.masterKey"))
    .getOrElse(
      throw new IllegalStateException(
        "encryption master key not configured (encryption.masterKey)"
      )
    )

  val service = new EncryptionService(masterKey)

  val decrypted: String = service.decrypt(encrypted) match {
    case Right(value) => value
    case Left(err)    =>
      logger.error(s"Decryption failed: ${err.getMessage}")
      sys.exit(1)
  }

  val enriched: JsObject =
    try {
      val decryptedJson = Json.parse(decrypted)
      originalJson + ("data" -> decryptedJson)
    } catch {
      case _: Throwable =>
        originalJson + ("data" -> JsString(decrypted))
    }

  // Print decrypted JSON using logger
  logger.info("\n" + Json.prettyPrint(enriched))
}

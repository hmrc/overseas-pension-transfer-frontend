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

package models.requests

import models.{PstrNumber, QtNumber, QtStatus}
import play.api.mvc.Request

final case class GetSpecificData(
    transferReference: Option[String], // exactly one of transferReference or qtNumber should be present
    qtNumber: Option[QtNumber],
    pstr: PstrNumber,
    qtStatus: QtStatus,
    version: Option[String]
  )

object GetSpecificDataParser {

  def fromRequest[A](req: Request[A]): Either[String, Option[GetSpecificData]] = {
    val q                 = req.queryString.view.mapValues(_.headOption).toMap
    val transferReference = q.get("transferReference").flatten.filter(_.nonEmpty)
    val qt                = q.get("qtNumber").flatten.map(s => QtNumber(s))
    val pstr              = q.get("pstr").flatten.map(s => PstrNumber(s))
    val status            = q.get("qtStatus").flatten.flatMap(QtStatus.parse)
    val version           = q.get("versionNumber").flatten

    val hasId = transferReference.isDefined || qt.isDefined
    if (!hasId) {
      Right(None)
    } else {
      (pstr, status) match {
        case (None, _)          => Left("Missing or invalid pstr")
        case (_, None)          => Left("Missing or invalid qtStatus")
        case (Some(p), Some(s)) =>
          Right(Some(GetSpecificData(transferReference, qt, p, s, version)))
      }
    }
  }
}

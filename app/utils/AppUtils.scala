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

import queries.DateSubmittedQuery
import queries.QtNumberQuery
import utils.DateTimeFormats.localDateTimeFormatter
import models.QtNumber
import models.SessionData
import models.UserAnswers
import java.time.ZoneId

trait AppUtils {

  def memberFullName(userAnswers: UserAnswers): String =
    userAnswers
      .get(pages.memberDetails.MemberNamePage)
      .map(_.fullName)
      .getOrElse("Undefined Undefined")

  def qtNumber(sessionData: SessionData): QtNumber =
    sessionData
      .get(QtNumberQuery)
      .getOrElse(QtNumber.empty)

  def dateTransferSubmitted(sessionData: SessionData): String =
    sessionData.get(DateSubmittedQuery).fold("Transfer not submitted") { instant =>
      val dateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime
      dateTime.format(localDateTimeFormatter)
    }

  def formatUkPostcode(raw: String): String = {
    val formated          = raw.trim.toUpperCase.replaceAll("\\s+", "")
    val (outcode, incode) = formated.splitAt(formated.length - 3)
    s"$outcode $incode"
  }
}

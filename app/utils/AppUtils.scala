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

import models.{QtNumber, SessionData, UserAnswers}
import pages.memberDetails.MemberNamePage
import queries.{DateSubmittedQuery, QtNumberQuery}

import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

trait AppUtils {

  def memberFullName(sessionData: SessionData): String = {
    sessionData.get(MemberNamePage).fold("Undefined Undefined")(_.fullName)
  }

  def qtNumber(sessionData: SessionData): QtNumber = {
    sessionData.get(QtNumberQuery)
      .getOrElse(QtNumber.empty)
  }

  def dateTransferSubmitted(sessionData: SessionData, userAnswers: Option[UserAnswers] = None): String = {
    def formatDate(instant: Instant): String = {
      DateTimeFormatter
        .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        .withZone(ZoneId.systemDefault())
        .format(instant)
    }
    sessionData.get(DateSubmittedQuery)
      .orElse(userAnswers.flatMap(ua =>
        (ua.data \ "transferDetails" \ "dateMemberTransferred").asOpt[String].map { dateStr =>
          java.time.LocalDate.parse(dateStr).atStartOfDay(ZoneId.systemDefault).toInstant
        }
      ))
      .fold("Transfer not submitted")(formatDate)
  }
}

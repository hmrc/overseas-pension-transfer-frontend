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

import base.SpecBase
import models.QtNumber
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.memberDetails.MemberNamePage
import queries.{DateSubmittedQuery, QtNumberQuery}

import java.time.format.{DateTimeFormatter, FormatStyle}

class AppUtilsSpec extends AnyFreeSpec with Matchers with SpecBase with AppUtils {

  "memberFullName" - {
    "must return full name when one is present in user answers" in {
      memberFullName(emptySessionData.set(MemberNamePage, testMemberName).success.value) mustBe "User McUser"
    }

    "must return Undefined Undefined when name not present in user answers" in {
      memberFullName(emptySessionData) mustBe "Undefined Undefined"
    }
  }

  "qtNumber" - {
    "must return QtNumber when one is present in user answers" in {
      qtNumber(emptySessionData.set(QtNumberQuery, testQtNumber).success.value) mustBe QtNumber("QT123456")
    }

    "must return QtNumber.empty when one is not present" in {
      qtNumber(emptySessionData) mustBe QtNumber.empty
    }
  }

  "dateTransferSubmitted" - {
    "must return date in String format when date is present" in {
      dateTransferSubmitted(emptySessionData.set(DateSubmittedQuery, testDateTransferSubmitted).success.value) mustBe
        testDateTransferSubmitted.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT))
    }

    "must return \'Transfer not submitted\' when date is not present" in {
      dateTransferSubmitted(emptySessionData) mustBe "Transfer not submitted"
    }
  }
}

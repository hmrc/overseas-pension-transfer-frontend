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

package forms

import forms.mappings.Mappings
import play.api.data.Form
import play.api.i18n.Messages

import java.time.{LocalDate, ZoneOffset}
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class MemberDateOfBirthFormProvider @Inject() extends Mappings {

  def minDate: LocalDate    = LocalDate of (1901, 1, 1)
  def maxDate: LocalDate    = LocalDate.now(ZoneOffset.UTC)
  private def dateFormatter = DateTimeFormatter.ofPattern("dd MM yyyy")

  def apply()(implicit messages: Messages): Form[LocalDate] =
    Form(
      "value" -> localDate(
        invalidCharacter = "common.dateInput.error.invalid.character",
        invalidKey       = "common.dateInput.error.invalid",
        allRequiredKey   = "memberDateOfBirth.error.required.all",
        twoRequiredKey   = "common.dateInput.error.required.two",
        requiredKey      = "common.dateInput.error.required"
      ).verifying(
        maxDate(maxDate, "common.dateInput.error.invalid.timeFrame", maxDate.format(dateFormatter)),
        minDate(minDate, "common.dateInput.error.invalid.timeFrame", minDate.format(dateFormatter))
      )
    )
}

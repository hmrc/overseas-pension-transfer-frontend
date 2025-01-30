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

class DateOfBirthFormProvider @Inject() extends Mappings {

  def minDate: LocalDate    =  LocalDate of(1900, 1, 1)
  def maxDate: LocalDate    = LocalDate.now(ZoneOffset.UTC)
  private def dateFormatter = DateTimeFormatter.ofPattern("DD MM yyyy")

  def apply()(implicit messages: Messages): Form[LocalDate] =
    Form(
      "value" -> localDate(
        invalidCharacter = "dateOfBirth.error.invalid.character",
        invalidKey     = "dateOfBirth.error.invalid",
        allRequiredKey = "dateOfBirth.error.required.all",
        twoRequiredKey = "dateOfBirth.error.required.two",
        requiredKey    = "dateOfBirth.error.required"
      ).verifying(
        maxDate(maxDate, "dateOfBirth.error.invalid", maxDate.format(dateFormatter)),
        minDate(minDate, "dateOfBirth.error.invalid", minDate.format(dateFormatter))
      )

    )
}

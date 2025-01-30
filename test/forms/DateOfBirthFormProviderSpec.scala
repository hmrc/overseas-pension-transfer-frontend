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

import java.time.{LocalDate, ZoneOffset}
import forms.behaviours.DateBehaviours
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

import java.time.format.DateTimeFormatter

class DateOfBirthFormProviderSpec extends DateBehaviours {

  private implicit val messages: Messages = stubMessages()
  private val form = new DateOfBirthFormProvider()()


  private val minDate = LocalDate.of(1900, 1, 1)
  private val maxDate = LocalDate.now(ZoneOffset.UTC)

  private def dateFormatter = DateTimeFormatter.ofPattern("dd MM yyyy")


  ".value" - {

    val validData = datesBetween(minDate, maxDate)

    behave like dateField(form, "value", validData)

    behave like dateFieldWithMax(
      form = form,
      key = "value",
      max = maxDate,
      formError = FormError(
        "value",
        "dateOfBirth.error.invalid",
        Seq(maxDate.format(dateFormatter))
      )
    )

    behave like dateFieldWithMin(
      form = form,
      key = "value",
      min = minDate,
      formError = FormError(
        "value",
        "dateOfBirth.error.invalid",
        Seq(minDate.format(dateFormatter))
      )
    )

    behave like mandatoryDateField(form, "value", "dateOfBirth.error.required.all")

  }
}

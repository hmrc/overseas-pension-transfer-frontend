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

package forms.memberDetails

import base.SpecBase
import forms.behaviours.DateBehaviours
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import utils.DateTimeFormats.dateInput

import java.time.LocalDate

class MemberDateOfBirthFormProviderSpec extends DateBehaviours with SpecBase {

  implicit private val messages: Messages = stubMessages()
  private val form                        = new MemberDateOfBirthFormProvider(clock)()

  private val minDate = LocalDate.of(1901, 1, 1)
  private val maxDate = today

  ".value" - {

    val validData = datesBetween(minDate, maxDate)

    behave like dateField(form, "value", validData)

    behave like dateFieldWithMax(
      form      = form,
      key       = "value",
      max       = maxDate,
      formError = FormError(
        "value",
        "common.dateInput.error.invalid.timeFrame",
        Seq(maxDate.format(dateInput))
      )
    )

    behave like dateFieldWithMin(
      form      = form,
      key       = "value",
      min       = minDate,
      formError = FormError(
        "value",
        "common.dateInput.error.invalid.timeFrame",
        Seq(minDate.format(dateInput))
      )
    )

    behave like mandatoryDateField(form, "value", "memberDateOfBirth.error.required.all")

  }
}

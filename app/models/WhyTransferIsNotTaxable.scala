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

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.checkbox._

sealed trait WhyTransferIsNotTaxable

object WhyTransferIsNotTaxable extends Enumerable.Implicits {

  case object IndividualIsEmployeeOccupational      extends WithName("individualIsEmployeeOccupational") with WhyTransferIsNotTaxable
  case object IndividualIsEmployedPublicService     extends WithName("individualIsEmployedPublicService") with WhyTransferIsNotTaxable
  case object IndividualIsEmployeeInternationalOrg  extends WithName("individualIsEmployeeInternationalOrg") with WhyTransferIsNotTaxable
  case object IndividualAndQROPSResidentSameCountry extends WithName("individualAndQROPSResidentSameCountry") with WhyTransferIsNotTaxable

  val values: Seq[WhyTransferIsNotTaxable] = Seq(
    IndividualIsEmployeeOccupational,
    IndividualIsEmployedPublicService,
    IndividualIsEmployeeInternationalOrg,
    IndividualAndQROPSResidentSameCountry
  )

  def checkboxItems(implicit messages: Messages): Seq[CheckboxItem] =
    values.zipWithIndex.map {
      case (value, index) =>
        CheckboxItemViewModel(
          content = Text(messages(s"whyTransferIsNotTaxable.${value.toString}")),
          fieldId = "value",
          index   = index,
          value   = value.toString
        )
    }

  implicit val enumerable: Enumerable[WhyTransferIsNotTaxable] =
    Enumerable(values.map(v => v.toString -> v): _*)
}

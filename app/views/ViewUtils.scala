/*
 * Copyright 2024 HM Revenue & Customs
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

package views

import models.{Address, AddressRecord, RecordSet, UkAddress}
import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import viewmodels.AddressField

object ViewUtils {

  def title(form: Form[_], title: String, section: Option[String] = None)(implicit messages: Messages): String =
    titleNoForm(
      title   = s"${errorPrefix(form)} ${messages(title)}",
      section = section
    )

  def titleNoForm(title: String, section: Option[String] = None)(implicit messages: Messages): String =
    s"${messages(title)} - ${section.fold("")(messages(_) + " - ")}${messages("service.name")} - ${messages("site.govuk")}"

  def errorPrefix(form: Form[_])(implicit messages: Messages): String = {
    if (form.hasErrors || form.hasGlobalErrors) messages("error.title.prefix") else ""
  }

  def addressRadios(addressRecords: RecordSet): Seq[RadioItem] =
    addressRecords.addresses.zipWithIndex.map {
      case (addressRecord: AddressRecord, index: Int) =>
        addressRecord match {
          case AddressRecord(id: String, address: UkAddress) =>
            def toOption[A](a: A)(implicit ev: AddressField[A]): Option[String] = ev.toOption(a)

            val formattedAddress = {
              List(
                toOption(address.line1),
                toOption(address.line2),
                toOption(address.line3),
                toOption(address.city),
                toOption(address.postcode)
              ).flatten.mkString(", ")
            }
            RadioItem(
              content = Text(formattedAddress),
              value   = Some(id),
              id      = Some(s"value_$index")
            )
        }
    }
}

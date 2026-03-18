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

package viewmodels

import models.address.Country
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SelectItem

case class CountrySelectViewModel(items: Seq[SelectItem])

object CountrySelectViewModel {

  def fromCountries(countries: Seq[Country])(implicit messages: Messages): CountrySelectViewModel = {

    val selectItems = {
      countries.map { country =>
        SelectItem(
          value = Some(country.code),
          text  = country.name
        )
      }
    }

    CountrySelectViewModel(selectItems)
  }
}

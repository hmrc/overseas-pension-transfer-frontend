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

package models.assets

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.checkbox._

sealed trait TypeOfAsset {
  val entryName: String
}

object TypeOfAsset extends Enumerable.Implicits {

  case object Cash extends WithName("cashAssets") with TypeOfAsset {
    override val entryName: String = "cashValue"
  }

  case object UnquotedShares extends WithName("unquotedShareAssets") with TypeOfAsset {
    override val entryName: String = "unquotedShares"
  }

  case object QuotedShares extends WithName("quotedShareAssets") with TypeOfAsset {
    override val entryName: String = "quotedShares"
  }

  case object Property extends WithName("propertyAsset") with TypeOfAsset {
    override val entryName: String = "propertyAssets"
  }

  case object Other extends WithName("otherAsset") with TypeOfAsset {
    override val entryName: String = "otherAssets"
  }

  val values: Seq[TypeOfAsset] = Seq(
    Cash,
    UnquotedShares,
    QuotedShares,
    Property,
    Other
  )

  implicit val ordering: Ordering[TypeOfAsset] =
    Ordering.by[TypeOfAsset, Int](t => values.indexOf(t))

  def checkboxItems(implicit messages: Messages): Seq[CheckboxItem] =
    values.zipWithIndex.map {
      case (value, index) =>
        CheckboxItemViewModel(
          content = Text(messages(s"typeOfAsset.${value.toString}")),
          fieldId = "value",
          index   = index,
          value   = value.toString
        )
    }

  implicit val enumerable: Enumerable[TypeOfAsset] =
    Enumerable(values.map(v => v.toString -> v): _*)
}

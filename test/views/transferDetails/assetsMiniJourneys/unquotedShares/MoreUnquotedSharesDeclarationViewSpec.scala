/*
 * Copyright 2026 HM Revenue & Customs
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

package views.transferDetails.assetsMiniJourneys.unquotedShares

import forms.transferDetails.assetsMiniJourneys.unquotedShares.MoreUnquotedSharesDeclarationFormProvider
import models.NormalMode
import play.api.data.FormError
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.addtoalist.ListItem
import views.html.transferDetails.assetsMiniJourneys.unquotedShares.MoreUnquotedSharesDeclarationView
import views.utils.ViewBaseSpec

class MoreUnquotedSharesDeclarationViewSpec extends ViewBaseSpec {

  private val view         = applicationBuilder().injector().instanceOf[MoreUnquotedSharesDeclarationView]
  private val formProvider = applicationBuilder().injector().instanceOf[MoreUnquotedSharesDeclarationFormProvider]

  private val testAssets = Seq(
    ListItem("Share 1", "change-url-1", "remove-url-1"),
    ListItem("Share 2", "change-url-2", "remove-url-2")
  )

  "MoreUnquotedSharesDeclarationView" - {

    "show correct title" in {
      doc(view(formProvider(), testAssets, NormalMode).body)
        .getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("moreUnquotedSharesDeclaration.title")} - ${messages("service.name")} - GOV.UK"
    }

    "display dynamic heading with asset count" in {
      val heading = doc(view(formProvider(), testAssets, NormalMode).body)
        .getElementsByTag("h1").first()

      heading.text() mustBe messages("additionalAsset.common.title", testAssets.length, messages("unquotedSharesAmendContinue.text.plural"))
    }

    "display list with actions" in {
      val list = doc(view(formProvider(), testAssets, NormalMode).body)
        .getElementsByClass("hmrc-list-with-actions")

      list.size() mustBe 1
    }

    "display yes/no radio buttons" in {
      val radios = doc(view(formProvider(), testAssets, NormalMode).body)
        .getElementsByClass("govuk-radios__item")

      radios.size() mustBe 2
    }

    behave like pageWithSubmitButton(view(formProvider(), testAssets, NormalMode), "site.saveAndContinue")

    behave like pageWithErrors(
      view(formProvider().withError(FormError("value", "moreUnquotedSharesDeclaration.error.required")), testAssets, NormalMode),
      "value",
      "moreUnquotedSharesDeclaration.error.required"
    )
  }
}

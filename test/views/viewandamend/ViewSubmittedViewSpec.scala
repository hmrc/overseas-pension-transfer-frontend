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

package views.viewandamend

import models.NormalMode
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import views.html.viewandamend.ViewSubmittedView
import views.utils.ViewBaseSpec

class ViewSubmittedViewSpec extends ViewBaseSpec {

  private val view = applicationBuilder().injector().instanceOf[ViewSubmittedView]

  private val emptySummaryList = SummaryList()
  private val qtNumber         = "QT123456"
  private val memberName       = "John Doe"

  "ViewSubmittedView when not amending" - {

    "show correct title" in {
      doc(view(
        NormalMode,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        qtNumber,
        memberName,
        isAmend   = false,
        isChanged = false
      ).body).getElementsByTag("title").eachText().get(0) mustBe
        s"${messages("viewSubmitted.amendTitle")} - ${messages("service.name")} - GOV.UK"
    }

    "display correct heading with member name" in {
      val heading = doc(view(
        NormalMode,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        qtNumber,
        memberName,
        isAmend   = false,
        isChanged = false
      ).body).getElementsByTag("h1").first()

      heading.text() mustBe messages("viewSubmitted.heading", memberName)
    }

    "display QT number as caption" in {
      val caption = doc(view(
        NormalMode,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        qtNumber,
        memberName,
        isAmend   = false,
        isChanged = false
      ).body).getElementsByClass("govuk-caption-l").first()

      caption.text() mustBe qtNumber
    }

    "display all section headings" in {
      val headings = doc(view(
        NormalMode,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        qtNumber,
        memberName,
        isAmend   = false,
        isChanged = false
      ).body).getElementsByClass("govuk-heading-m")

      headings.size() mustBe 4
      headings.get(0).text() mustBe messages("common.memberDetails.heading")
      headings.get(1).text() mustBe messages("common.transferDetails.heading")
      headings.get(2).text() mustBe messages("common.qropsDetails.heading")
      headings.get(3).text() mustBe messages("common.qropsSchemeManagerDetails.heading")
    }

    "display all summary lists" in {
      val summaryLists = doc(view(
        NormalMode,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        qtNumber,
        memberName,
        isAmend   = false,
        isChanged = false
      ).body).getElementsByClass("govuk-summary-list")

      summaryLists.size() mustBe 5
    }

    "not display continue button when not amending" in {
      val buttons = doc(view(
        NormalMode,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        qtNumber,
        memberName,
        isAmend   = false,
        isChanged = false
      ).body).getElementsByClass("govuk-button")

      buttons.size() mustBe 0
    }
  }

  "ViewSubmittedView when amending with changes" - {

    "display amend heading without member name" in {
      val heading = doc(view(
        NormalMode,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        qtNumber,
        memberName,
        isAmend   = true,
        isChanged = true
      ).body).getElementsByTag("h1").first()

      heading.text() mustBe messages("viewSubmitted.amendHeading")
    }

    "display continue button when amending with changes" in {
      val buttons = doc(view(
        NormalMode,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        qtNumber,
        memberName,
        isAmend   = true,
        isChanged = true
      ).body).select("a.govuk-button")

      buttons.size() mustBe 1
      buttons.first().text() must include(messages("site.continue"))
    }

    "display all section headings when amending" in {
      val headings = doc(view(
        NormalMode,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        qtNumber,
        memberName,
        isAmend   = true,
        isChanged = true
      ).body).getElementsByClass("govuk-heading-m")

      headings.size() mustBe 4
    }
  }

  "ViewSubmittedView when amending without changes" - {

    "not display continue button when amending without changes" in {
      val buttons = doc(view(
        NormalMode,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        qtNumber,
        memberName,
        isAmend   = true,
        isChanged = false
      ).body).select("a.govuk-button")

      buttons.size() mustBe 0
    }

    "still display amend heading" in {
      val heading = doc(view(
        NormalMode,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        emptySummaryList,
        qtNumber,
        memberName,
        isAmend   = true,
        isChanged = false
      ).body).getElementsByTag("h1").first()

      heading.text() mustBe messages("viewSubmitted.amendHeading")
    }
  }
}

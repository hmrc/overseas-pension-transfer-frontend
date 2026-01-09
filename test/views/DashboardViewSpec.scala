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

package views

import config.FrontendAppConfig
import controllers.routes
import models.QtStatus.{AmendInProgress, InProgress, Submitted}
import models.{AllTransfersItem, QtNumber}
import org.jsoup.select.Elements
import play.twirl.api.Html
import viewmodels.{PaginatedAllTransfersViewModel, SearchBarViewModel}
import views.html.DashboardView
import views.utils.ViewBaseSpec

import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

class DashboardViewSpec extends ViewBaseSpec {

  implicit val appConfig: FrontendAppConfig = applicationBuilder().injector().instanceOf[FrontendAppConfig]

  val dashboardView: DashboardView = applicationBuilder().injector().instanceOf[DashboardView]

  val pageUrl: Int => String = page => routes.DashboardController.onPageLoad(page, None).url

  private val searchBarViewModel = SearchBarViewModel(
    label  = messages("dashboard.search.heading"),
    action = routes.DashboardController.onPageLoad().url
  )

  private val paginatedAllTransfersViewModel =
    PaginatedAllTransfersViewModel.build(
      Seq(
        AllTransfersItem(testQtNumber, Some("001"), Some(Submitted), None, Some("Firstname1"), Some("Surname1"), None, Some(now), None, None),
        AllTransfersItem(userAnswersTransferNumber, None, Some(InProgress), None, Some("Firstname2"), Some("Surname2"), None, Some(now), None, None),
        AllTransfersItem(QtNumber("QT987654"), Some("001"), Some(AmendInProgress), None, Some("Firstname3"), Some("Surname3"), None, Some(now), None, None)
      ),
      1,
      appConfig.transfersPerPage,
      pageUrl
    )

  private val paginatedNoTransfersViewModel =
    PaginatedAllTransfersViewModel.build(
      Seq(),
      1,
      appConfig.transfersPerPage,
      pageUrl
    )

  private val paginatedAllTransfersWithLock =
    PaginatedAllTransfersViewModel.build(
      Seq(
        AllTransfersItem(userAnswersTransferNumber, None, Some(InProgress), None, Some("Firstname"), Some("Surname"), None, Some(now), None, None)
      ),
      1,
      appConfig.transfersPerPage,
      pageUrl,
      Some("Firstname Surname")
    )

  private val fullView = dashboardView(
    "Scheme Name",
    "/what-will-be-needed",
    paginatedAllTransfersViewModel,
    searchBarViewModel,
    Seq(),
    "/mps-link",
    isSearch = false,
    "/pension-scheme-link",
    Html("")
  )

  private val viewWithNoTransfers = dashboardView(
    "Scheme Name",
    "/what-will-be-needed",
    paginatedNoTransfersViewModel,
    searchBarViewModel,
    Seq(),
    "/mps-link",
    isSearch = false,
    "/pension-scheme-link",
    Html("")
  )

  private val viewWithLockWarning = dashboardView(
    "Scheme Name",
    "/what-will-be-needed",
    paginatedAllTransfersWithLock,
    searchBarViewModel,
    Seq(),
    "/mps-link",
    isSearch = false,
    "/pension-scheme-link",
    Html("")
  )

  val formattedLastUpdated: String = {
    val dateFormatter = DateTimeFormatter.ofPattern("d MMMM uuuu")
      .withLocale(Locale.UK)
    val timeFormatter = DateTimeFormatter.ofPattern("h:mma")
      .withLocale(Locale.UK)

    s"${dateFormatter.format(now.atZone(ZoneOffset.UTC))} ${timeFormatter.format(now.atZone(ZoneOffset.UTC))}"
  }

  "full dashboard view" - {
    "show correct title" in {
      doc(fullView.body).getElementsByTag("title").eachText().get(0) mustBe s"${messages("dashboard.allTransfers.page", "1", "1")} – Report a transfer to a qualifying recognised overseas pension scheme (QROPS) – GOV.UK"
    }

    behave like pageWithH1(fullView, "dashboard.heading")
    behave like pageWithHeadings(fullView, "h2", "Scheme Name", "dashboard.search.heading")
    behave like pageWithLinks(
      fullView,
      ("dashboard.linkText", "/what-will-be-needed"),
      (
        "Firstname1 Surname1",
        "/dashboard/transfer-report?transferId=QT123456&qtStatus=Submitted&versionNumber=001&memberName=Firstname1+Surname1&currentPage=1"
      ),
      (
        "Firstname2 Surname2",
        s"/dashboard/transfer-report?transferId=${userAnswersTransferNumber.value}&qtStatus=InProgress&memberName=Firstname2+Surname2&currentPage=1"
      ),
      (
        "Firstname3 Surname3",
        "/dashboard/transfer-report?transferId=QT987654&qtStatus=AmendInProgress&versionNumber=001&memberName=Firstname3+Surname3&currentPage=1"
      ),
      ("Return to Scheme Name pension scheme", "/pension-scheme-link")
    )

    "correctly display the transfer records in a table" - {
      val table: Elements = doc(fullView.body).getElementsByTag("table")

      "display correct table headings" in {
        table.select(".govuk-table__header").eachText().toArray() mustBe
          Array(
            messages("dashboard.allTransfers.head.member"),
            messages("dashboard.allTransfers.head.status"),
            messages("dashboard.allTransfers.head.reference"),
            messages("dashboard.allTransfers.head.updated")
          )
      }

      "display correct table rows" in {
        val rows = table.select(".govuk-table__row").asList()

        rows.get(1).getElementsByTag("td").eachText().toArray() mustBe
          Array(
            "Firstname1 Surname1",
            "Submitted to HMRC",
            testQtNumber.value,
            formattedLastUpdated
          )

        rows.get(2).getElementsByTag("td").eachText().toArray() mustBe
          Array(
            "Firstname2 Surname2",
            "In progress",
            "Available after submission",
            formattedLastUpdated
          )

        rows.get(3).getElementsByTag("td").eachText().toArray() mustBe
          Array(
            "Firstname3 Surname3",
            "Amendment in progress",
            "QT987654",
            formattedLastUpdated
          )
      }
    }
  }

  "No transfer records dashboard view" - {
    "show correct title" in {
      doc(viewWithNoTransfers.body).getElementsByTag("title").eachText().get(0) mustBe s"${messages("dashboard.allTransfers.page", "1", "1")} – Report a transfer to a qualifying recognised overseas pension scheme (QROPS) – GOV.UK"
    }

    behave like pageWithH1(viewWithNoTransfers, "dashboard.heading")
    behave like pageWithHeadings(viewWithNoTransfers, "h2", "Scheme Name", "dashboard.search.heading")
    behave like pageWithLinks(
      viewWithNoTransfers,
      ("dashboard.linkText", "/what-will-be-needed"),
      ("Return to Scheme Name pension scheme", "/pension-scheme-link")
    )
    behave like pageWithText(
      viewWithNoTransfers,
      "dashboard.linkText",
      "dashboard.allTransfers.empty",
      "Return to Scheme Name pension scheme"
    )
  }

  "View with lock warning" - {
    "display lock warning" in {
      doc(viewWithLockWarning.body).getElementById("govuk-lock-banner-title").text() mustBe messages("dashboard.lock.title")
      doc(viewWithLockWarning.body).getElementsByClass("govuk-notification-banner__content").text() mustBe
        s"${messages("dashboard.lock.warning", "Firstname Surname")} ${messages("dashboard.lock.hide")}."
    }
  }

  "View with expiry warning" - {

    val messageDate = {
      val expiryDate = now.atZone(java.time.ZoneId.systemDefault()).toLocalDate().plusDays(25)

      DateTimeFormatter.ofPattern("d MMMM yyyy")
        .withLocale(Locale.UK)
        .format(expiryDate)
    }

    "display expiry warning for in progress record" in {
      val viewWithExpiringRecord = dashboardView(
        "Scheme Name",
        "/what-will-be-needed",
        paginatedAllTransfersViewModel,
        searchBarViewModel,
        Seq(AllTransfersItem(userAnswersTransferNumber, None, Some(InProgress), None, Some("Firstname2"), Some("Surname2"), None, Some(now), None, None)),
        "/mps-link",
        isSearch = false,
        "/pension-scheme-link",
        Html("")
      )

      doc(viewWithExpiringRecord.body).getElementById("govuk-notification-banner-title").text() mustBe messages("dashboard.banner.title")
      doc(viewWithExpiringRecord.body).getElementsByClass(
        "govuk-notification-banner__content"
      ).text() mustBe s"${messages("dashboard.banner.inProgress.text", "Firstname2 Surname2", messageDate)} ${messages("dashboard.banner.linkText")}"
    }

    "display expiry warning for amendment in progress record" in {
      val viewWithExpiringRecord = dashboardView(
        "Scheme Name",
        "/what-will-be-needed",
        paginatedAllTransfersViewModel,
        searchBarViewModel,
        Seq(AllTransfersItem(userAnswersTransferNumber, None, Some(AmendInProgress), None, Some("Firstname2"), Some("Surname2"), None, Some(now), None, None)),
        "/mps-link",
        isSearch = false,
        "/pension-scheme-link",
        Html("")
      )

      doc(viewWithExpiringRecord.body).getElementById("govuk-notification-banner-title").text() mustBe messages("dashboard.banner.title")
      doc(viewWithExpiringRecord.body).getElementsByClass(
        "govuk-notification-banner__content"
      ).text() mustBe s"${messages("dashboard.banner.updateInProgress.text", "Firstname2 Surname2", messageDate)} ${messages("dashboard.banner.linkText")}"
    }
  }
}

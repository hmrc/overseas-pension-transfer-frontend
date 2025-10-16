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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.test.FakeRequest
import controllers.routes

class TransferReportQueryParamsSpec extends AnyFreeSpec with Matchers {

  "fromRequest" - {

    "must correctly parse all parameters from query string" in {
      val request = FakeRequest(
        "GET",
        "/report-transfer" +
          "?transferReference=TR123" +
          "&qtReference=QT456" +
          "&qtStatus=Submitted" +
          "&pstr=12345678AB" +
          "&versionNumber=7" +
          "&memberName=John+Doe" +
          "&currentPage=3"
      )

      val result = TransferReportQueryParams.fromRequest(request)

      result.transferReference mustBe Some("TR123")
      result.qtReference mustBe Some("QT456")
      result.qtStatus mustBe Some(QtStatus.Submitted)
      result.pstr mustBe Some(PstrNumber("12345678AB"))
      result.versionNumber mustBe Some("7")
      result.memberName mustBe "John Doe"
      result.currentPage mustBe 3
    }

    "must default memberName to '-' and currentPage to 1 when not present" in {
      val request = FakeRequest("GET", "/report-transfer?transferReference=TR123")

      val result = TransferReportQueryParams.fromRequest(request)

      result.transferReference mustBe Some("TR123")
      result.qtReference mustBe None
      result.qtStatus mustBe None
      result.pstr mustBe None
      result.versionNumber mustBe None
      result.memberName mustBe "-"
      result.currentPage mustBe 1
    }

    "must ignore invalid qtStatus values" in {
      val request = FakeRequest("GET", "/report-transfer?qtStatus=InvalidStatus")

      val result = TransferReportQueryParams.fromRequest(request)

      result.qtStatus mustBe None
    }
  }

  "toQueryString" - {

    "must correctly build encoded query string (including pstr & versionNumber)" in {
      val params = TransferReportQueryParams(
        transferReference = Some("TR 123"),
        qtReference       = Some("QT/456"),
        qtStatus          = Some(QtStatus.Submitted),
        pstr              = Some(PstrNumber("12345678AB")),
        versionNumber     = Some("v 7"),
        memberName        = "John Doe",
        currentPage       = 2
      )

      val result = TransferReportQueryParams.toQueryString(params)

      result must startWith("?")
      result must include("transferReference=TR+123")
      result must include("qtReference=QT%2F456")
      result must include("qtStatus=Submitted")
      result must include("pstr=12345678AB")
      result must include("versionNumber=v+7")
      result must include("memberName=John+Doe")
      result must include("currentPage=2")
    }

    "must omit missing optional fields but always include memberName and currentPage" in {
      val params = TransferReportQueryParams(
        transferReference = None,
        qtReference       = None,
        qtStatus          = None,
        pstr              = None,
        versionNumber     = None,
        memberName        = "Jane",
        currentPage       = 1
      )

      val result = TransferReportQueryParams.toQueryString(params)

      result mustBe "?memberName=Jane&currentPage=1"
    }
  }

  "toUrl" - {

    "must build full URL to DashboardController.onTransferClick with query params" in {
      val params = TransferReportQueryParams(
        transferReference = Some("TR001"),
        qtReference       = Some("QT002"),
        qtStatus          = Some(QtStatus.InProgress),
        pstr              = Some(PstrNumber("12345678AB")),
        versionNumber     = Some("7"),
        memberName        = "Malcolm Mendes",
        currentPage       = 4
      )

      val result = TransferReportQueryParams.toUrl(params)

      result must startWith(routes.DashboardController.onTransferClick().url)
      result must include("transferReference=TR001")
      result must include("qtReference=QT002")
      result must include("qtStatus=InProgress")
      result must include("pstr=12345678AB")
      result must include("versionNumber=7")
      result must include("memberName=Malcolm+Mendes")
      result must include("currentPage=4")
    }
  }
}

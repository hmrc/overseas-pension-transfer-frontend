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

import base.SpecBase
import controllers.routes
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.test.FakeRequest

class TransferReportQueryParamsSpec extends AnyFreeSpec with Matchers with SpecBase {

  "fromRequest" - {

    "must correctly parse all parameters from query string" in {
      val request = FakeRequest(
        "GET",
        "/report-transfer" +
          "?transferId=QT456321" +
          "&qtStatus=Submitted" +
          "&pstr=12345678AB" +
          "&versionNumber=7" +
          "&memberName=John+Doe" +
          "&currentPage=3"
      )

      val result = TransferReportQueryParams.fromRequest(request)

      result.transferId mustBe Some(QtNumber("QT456321"))
      result.qtStatus mustBe Some(QtStatus.Submitted)
      result.pstr mustBe Some(PstrNumber("12345678AB"))
      result.versionNumber mustBe Some("7")
      result.memberName mustBe "John Doe"
      result.currentPage mustBe 3
    }

    "must default memberName to '-' and currentPage to 1 when not present" in {
      val request = FakeRequest("GET", s"/report-transfer?transferId=${userAnswersTransferNumber.value}")

      val result = TransferReportQueryParams.fromRequest(request)

      result.transferId mustBe Some(userAnswersTransferNumber)
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
        transferId    = Some(QtNumber("QT123456")),
        qtStatus      = Some(QtStatus.Submitted),
        pstr          = Some(PstrNumber("12345678AB")),
        versionNumber = Some("v 7"),
        memberName    = "John Doe",
        currentPage   = 2
      )

      val result = TransferReportQueryParams.toQueryString(params)

      result must startWith("?")
      result must include("transferId=QT123456")
      result must include("qtStatus=Submitted")
      result must include("pstr=12345678AB")
      result must include("versionNumber=v+7")
      result must include("memberName=John+Doe")
      result must include("currentPage=2")
    }

    "must omit missing optional fields but always include memberName and currentPage" in {
      val params = TransferReportQueryParams(
        transferId    = None,
        qtStatus      = None,
        pstr          = None,
        versionNumber = None,
        memberName    = "Jane",
        currentPage   = 1
      )

      val result = TransferReportQueryParams.toQueryString(params)

      result mustBe "?memberName=Jane&currentPage=1"
    }
  }

  "toUrl" - {

    "must build full URL to DashboardController.onTransferClick with query params" in {
      val params = TransferReportQueryParams(
        transferId    = Some(QtNumber("QT002007")),
        qtStatus      = Some(QtStatus.AmendInProgress),
        pstr          = Some(PstrNumber("12345678AB")),
        versionNumber = Some("7"),
        memberName    = "Malcolm Mendes",
        currentPage   = 4
      )

      val result = TransferReportQueryParams.toUrl(params)

      result must startWith(routes.DashboardController.onTransferClick().url)
      result must include("transferId=QT002007")
      result must include("qtStatus=AmendInProgress")
      result must include("pstr=12345678AB")
      result must include("versionNumber=7")
      result must include("memberName=Malcolm+Mendes")
      result must include("currentPage=4")
    }
  }
}

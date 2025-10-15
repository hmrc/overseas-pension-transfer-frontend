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

package pages

import controllers.routes
import models.{DashboardData, QtStatus, TransferReportQueryParams}
import play.api.Logging
import play.api.mvc.Call
import queries.PensionSchemeDetailsQuery

object DashboardPage extends Page with Logging {

  def nextPage(dd: DashboardData, qtStatus: Option[QtStatus], params: Option[TransferReportQueryParams]): Call =
    qtStatus match {
      case Some(QtStatus.InProgress) => ??? // TODO: Replace with In-progress controller redirect

      case Some(QtStatus.Compiled) | Some(QtStatus.Submitted) =>
        val data = params.getOrElse(throw new IllegalArgumentException("Submitted transfers require query params"))
        controllers.routes.ViewSubmittedController.fromDashboard(
          data.qtReference.get,
          data.pstr.get,
          qtStatus.get,
          data.versionNumber.get
        )
      case _                                                  =>
        dd.get(PensionSchemeDetailsQuery) match {
          case Some(_) =>
            routes.WhatWillBeNeededController.onPageLoad()
          case _       =>
            controllers.auth.routes.UnauthorisedController.onPageLoad()
        }
    }
}

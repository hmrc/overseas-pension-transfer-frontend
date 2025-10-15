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
import play.api.mvc.Call
import queries.PensionSchemeDetailsQuery

object DashboardPage extends Page {

  def nextPage(dd: DashboardData, qtStatus: Option[QtStatus], transferReference: Option[String]): Call =
    qtStatus match {
      case Some(QtStatus.InProgress) =>
        transferReference.fold(routes.JourneyRecoveryController.onPageLoad()) {
          transferRef => routes.TaskListController.fromDashboard(transferRef)
        }

      case Some(QtStatus.Compiled) | Some(QtStatus.Submitted) => ??? // TODO: Replace with Submitted controller redirect
      case _                                                  =>
        dd.get(PensionSchemeDetailsQuery) match {
          case Some(_) =>
            routes.WhatWillBeNeededController.onPageLoad()
          case _       =>
            controllers.auth.routes.UnauthorisedController.onPageLoad()
        }
    }
}

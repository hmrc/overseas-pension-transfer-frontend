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

package controllers

import com.google.inject.Inject
import controllers.actions.{DataRetrievalAction, IdentifierAction, SchemeDataAction}
import models.FinalCheckMode
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AppUtils
import viewmodels.checkAnswers.memberDetails.MemberDetailsSummary
import viewmodels.checkAnswers.qropsDetails.QROPSDetailsSummary
import viewmodels.checkAnswers.qropsSchemeManagerDetails.SchemeManagerDetailsSummary
import viewmodels.checkAnswers.schemeOverview.SchemeDetailsSummary
import viewmodels.checkAnswers.transferDetails.TransferDetailsSummary
import viewmodels.govuk.summarylist._
import views.html.ViewSubmittedView

import scala.concurrent.ExecutionContext

class ViewSubmittedController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    schemeData: SchemeDataAction,
    getData: DataRetrievalAction,
    val controllerComponents: MessagesControllerComponents,
    view: ViewSubmittedView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with AppUtils with Logging {

  def onPageLoad(qtNumber: String, pstr: String, status: String, versionNumber: String, dateSubmitted: String): Action[AnyContent] =
    (identify andThen schemeData andThen getData) {
      implicit request =>
        val schemeSummaryList               = SummaryListViewModel(SchemeDetailsSummary.rows(FinalCheckMode, "schemeName", request.dateTransferSubmitted))
        val memberDetailsSummaryList        = SummaryListViewModel(MemberDetailsSummary.rows(FinalCheckMode, request.userAnswers))
        val transferDetailsSummaryList      = SummaryListViewModel(TransferDetailsSummary.rows(FinalCheckMode, request.userAnswers))
        val qropsDetailsSummaryList         = SummaryListViewModel(QROPSDetailsSummary.rows(FinalCheckMode, request.userAnswers))
        val schemeManagerDetailsSummaryList = SummaryListViewModel(SchemeManagerDetailsSummary.rows(FinalCheckMode, request.userAnswers))

        Ok(view(
          schemeSummaryList,
          memberDetailsSummaryList,
          transferDetailsSummaryList,
          qropsDetailsSummaryList,
          schemeManagerDetailsSummaryList
        ))
    }
}

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
import models.{FinalCheckMode, PstrNumber, QtStatus, SessionData, TransferReportQueryParams}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{__, Json}
import play.api.mvc.Results.Redirect
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AppUtils
import viewmodels.checkAnswers.memberDetails.MemberDetailsSummary
import viewmodels.checkAnswers.qropsDetails.QROPSDetailsSummary
import viewmodels.checkAnswers.qropsSchemeManagerDetails.SchemeManagerDetailsSummary
import viewmodels.checkAnswers.schemeOverview.SchemeDetailsSummary
import viewmodels.checkAnswers.transferDetails.TransferDetailsSummary
import viewmodels.govuk.summarylist._
import views.html.ViewSubmittedView

import scala.concurrent.{ExecutionContext, Future}

class ViewSubmittedController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    schemeData: SchemeDataAction,
    getData: DataRetrievalAction,
    sessionRepository: SessionRepository,
    userAnswersService: UserAnswersService,
    val controllerComponents: MessagesControllerComponents,
    view: ViewSubmittedView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with AppUtils with Logging {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen schemeData andThen getData) {
      implicit request =>
        val schemeName                      = request.sessionData.schemeInformation.schemeName
        val schemeSummaryList               = SummaryListViewModel(SchemeDetailsSummary.rows(FinalCheckMode, schemeName, request.dateTransferSubmitted))
        val memberDetailsSummaryList        = SummaryListViewModel(MemberDetailsSummary.rows(FinalCheckMode, request.userAnswers, showChangeLinks = false))
        val transferDetailsSummaryList      = SummaryListViewModel(TransferDetailsSummary.rows(FinalCheckMode, request.userAnswers, showChangeLinks = false))
        val qropsDetailsSummaryList         = SummaryListViewModel(QROPSDetailsSummary.rows(FinalCheckMode, request.userAnswers, showChangeLinks = false))
        val schemeManagerDetailsSummaryList =
          SummaryListViewModel(SchemeManagerDetailsSummary.rows(FinalCheckMode, request.userAnswers, showChangeLinks = false))

        Ok(view(
          schemeSummaryList,
          memberDetailsSummaryList,
          transferDetailsSummaryList,
          qropsDetailsSummaryList,
          schemeManagerDetailsSummaryList
        ))
    }

  def fromDashboard(qtReference: String, pstr: PstrNumber, qtStatus: QtStatus, versionNumber: String): Action[AnyContent] =
    (identify andThen schemeData).async {
      implicit request =>
        userAnswersService.getExternalUserAnswers(None, Some(qtReference), pstr, qtStatus, Some(versionNumber)).flatMap {
          case Right(answers) =>
            val session = SessionData(
              request.authenticatedUser.internalId,
              qtReference,
              request.authenticatedUser.pensionSchemeDetails.get,
              request.authenticatedUser,
              Json.toJsObject(answers)
            )
            sessionRepository.set(session).map { _ =>
              Redirect(controllers.routes.ViewSubmittedController.onPageLoad())
            }
          case Left(_)        =>
            Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        }
    }
}

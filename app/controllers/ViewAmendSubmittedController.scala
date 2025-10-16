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
import controllers.actions.{IdentifierAction, SchemeDataAction}
import models.requests.IdentifierRequest
import models.{FinalCheckMode, PstrNumber, QtStatus, SessionData, UserAnswers}
import pages.memberDetails.MemberNamePage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result}
import play.twirl.api.HtmlFormat
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

import scala.concurrent.ExecutionContext

class ViewAmendSubmittedController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    schemeData: SchemeDataAction,
    userAnswersService: UserAnswersService,
    val controllerComponents: MessagesControllerComponents,
    view: ViewSubmittedView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with AppUtils with Logging {

  def view(qtReference: String, pstr: PstrNumber, qtStatus: QtStatus, versionNumber: String): Action[AnyContent] =
    (identify andThen schemeData).async {
      implicit request =>
        userAnswersService
          .getExternalUserAnswers(None, Some(qtReference), pstr, qtStatus, Some(versionNumber))
          .map {
            case Right(userAnswers) =>
              val sessionData = SessionData(
                request.authenticatedUser.internalId,
                qtReference,
                request.authenticatedUser.pensionSchemeDetails.get,
                request.authenticatedUser,
                Json.toJsObject(userAnswers)
              )
              Ok(renderView(sessionData, userAnswers))
            case Left(_)            =>
              Redirect(routes.JourneyRecoveryController.onPageLoad())
          }
    }

  def amend(qtReference: String, pstr: PstrNumber, qtStatus: QtStatus, versionNumber: String): Action[AnyContent] =
    (identify andThen schemeData).async {
      implicit request =>
        userAnswersService
          .getExternalUserAnswers(None, Some(qtReference), pstr, qtStatus, Some(versionNumber))
          .map {
            case Right(userAnswers) =>
              val sessionData = SessionData(
                request.authenticatedUser.internalId,
                qtReference,
                request.authenticatedUser.pensionSchemeDetails.get,
                request.authenticatedUser,
                Json.toJsObject(userAnswers)
              )
              Ok(renderView(sessionData, userAnswers))
            case Left(_)            =>
              Redirect(routes.JourneyRecoveryController.onPageLoad())
          }
    }

  private def renderView(sessionData: SessionData, userAnswers: UserAnswers)(implicit request: IdentifierRequest[_]): HtmlFormat.Appendable = {
    val schemeName                      = sessionData.schemeInformation.schemeName
    val schemeSummaryList               = SummaryListViewModel(SchemeDetailsSummary.rows(FinalCheckMode, schemeName, dateTransferSubmitted(sessionData)))
    val memberDetailsSummaryList        = SummaryListViewModel(MemberDetailsSummary.rows(FinalCheckMode, userAnswers, showChangeLinks = false))
    val transferDetailsSummaryList      = SummaryListViewModel(TransferDetailsSummary.rows(FinalCheckMode, userAnswers, showChangeLinks = false))
    val qropsDetailsSummaryList         = SummaryListViewModel(QROPSDetailsSummary.rows(FinalCheckMode, userAnswers, showChangeLinks = false))
    val schemeManagerDetailsSummaryList =
      SummaryListViewModel(SchemeManagerDetailsSummary.rows(FinalCheckMode, userAnswers, showChangeLinks = false))

    val memberName =
      userAnswers.get(MemberNamePage).map(_.fullName).get

    view(
      schemeSummaryList,
      memberDetailsSummaryList,
      transferDetailsSummaryList,
      qropsDetailsSummaryList,
      schemeManagerDetailsSummaryList,
      sessionData.transferId,
      memberName
    )
  }
}

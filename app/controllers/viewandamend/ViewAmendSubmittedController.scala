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

package controllers.viewandamend

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.{DataRetrievalAction, IdentifierAction, SchemeDataAction}
import controllers.viewandamend.routes
import models.authentication.{PsaUser, PspUser}
import models.requests.IdentifierRequest
import models.{AmendCheckMode, PstrNumber, QtNumber, QtStatus, SessionData, TransferId, UserAnswers}
import pages.memberDetails.MemberNamePage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.HtmlFormat
import services.UserAnswersService
import uk.gov.hmrc.mongo.lock.LockRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AppUtils
import viewmodels.checkAnswers.memberDetails.MemberDetailsSummary
import viewmodels.checkAnswers.qropsDetails.QROPSDetailsSummary
import viewmodels.checkAnswers.qropsSchemeManagerDetails.SchemeManagerDetailsSummary
import viewmodels.checkAnswers.schemeOverview.SchemeDetailsSummary
import viewmodels.checkAnswers.transferDetails.TransferDetailsSummary
import viewmodels.govuk.summarylist._
import views.html.viewandamend.ViewSubmittedView

import scala.concurrent.duration.DurationLong
import scala.concurrent.{ExecutionContext, Future}

class ViewAmendSubmittedController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    schemeData: SchemeDataAction,
    userAnswersService: UserAnswersService,
    getData: DataRetrievalAction,
    val controllerComponents: MessagesControllerComponents,
    view: ViewSubmittedView,
    lockRepository: LockRepository,
    appConfig: FrontendAppConfig
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with AppUtils with Logging {

  def view(qtReference: TransferId, pstr: PstrNumber, qtStatus: QtStatus, versionNumber: String): Action[AnyContent] =
    (identify andThen schemeData).async {
      implicit request =>
        qtReference match {
          case QtNumber(_) =>
            userAnswersService
              .getExternalUserAnswers(qtReference, pstr, qtStatus, Some(versionNumber))
              .map {
                case Right(userAnswers) =>
                  val sessionData = SessionData(
                    request.authenticatedUser.internalId,
                    qtReference,
                    request.authenticatedUser.pensionSchemeDetails.get,
                    request.authenticatedUser,
                    Json.toJsObject(userAnswers)
                  )
                  Ok(renderView(sessionData, userAnswers, isAmend = false))
                case Left(_)            =>
                  Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
              }
          case _           => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }

    }

  def fromDraft(qtReference: TransferId, pstr: PstrNumber, qtStatus: QtStatus, versionNumber: String): Action[AnyContent] =
    (identify andThen schemeData).async {
      implicit request =>
        val owner = request.authenticatedUser match {
          case PsaUser(psaId, _, _, _) => psaId.value
          case PspUser(pspId, _, _, _) => pspId.value
        }

        lockRepository.takeLock(qtReference.value, owner, appConfig.dashboardLockTtl.seconds) flatMap {
          case Some(_) =>
            userAnswersService.getExternalUserAnswers(qtReference, pstr, qtStatus, Some(versionNumber))
              .map {
                case Right(userAnswers) =>
                  val sessionData = SessionData(
                    request.authenticatedUser.internalId,
                    qtReference,
                    request.authenticatedUser.pensionSchemeDetails.get,
                    request.authenticatedUser,
                    Json.toJsObject(userAnswers)
                  )
                  Redirect(routes.ViewAmendSubmittedController.view(qtReference, pstr, qtStatus, versionNumber))
                case Left(_)            =>
                  Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
              }
          case None    =>
            Future.successful(
              Redirect(routes.SubmittedTransferSummaryController.onPageLoad(qtReference, pstr, qtStatus, versionNumber))
                .flashing("lockWarning" -> "")
            )

        }

    }

  def amend(): Action[AnyContent] =
    (identify andThen schemeData andThen getData) { implicit dr =>
      implicit val idReq: IdentifierRequest[_] =
        IdentifierRequest(dr.request, dr.authenticatedUser)
      Ok(renderView(dr.sessionData, dr.userAnswers, isAmend = true))
    }

  private def renderView(
      sessionData: SessionData,
      userAnswers: UserAnswers,
      isAmend: Boolean
    )(implicit request: IdentifierRequest[_]
    ): HtmlFormat.Appendable = {
    val schemeName                      = sessionData.schemeInformation.schemeName
    val schemeSummaryList               = SummaryListViewModel(SchemeDetailsSummary.rows(AmendCheckMode, schemeName, dateTransferSubmitted(sessionData)))
    val memberDetailsSummaryList        = if (isAmend) {
      SummaryListViewModel(MemberDetailsSummary.amendRows(AmendCheckMode, userAnswers))
    } else {
      SummaryListViewModel(MemberDetailsSummary.rows(AmendCheckMode, userAnswers, showChangeLinks = false))
    }
    val transferDetailsSummaryList      = SummaryListViewModel(TransferDetailsSummary.rows(AmendCheckMode, userAnswers, showChangeLinks = isAmend))
    val qropsDetailsSummaryList         = SummaryListViewModel(QROPSDetailsSummary.rows(AmendCheckMode, userAnswers, showChangeLinks = isAmend))
    val schemeManagerDetailsSummaryList =
      SummaryListViewModel(SchemeManagerDetailsSummary.rows(AmendCheckMode, userAnswers, showChangeLinks = isAmend))

    val memberName =
      userAnswers.get(MemberNamePage).map(_.fullName).getOrElse("")

    view(
      schemeSummaryList,
      memberDetailsSummaryList,
      transferDetailsSummaryList,
      qropsDetailsSummaryList,
      schemeManagerDetailsSummaryList,
      sessionData.transferId.value,
      memberName
    )
  }
}

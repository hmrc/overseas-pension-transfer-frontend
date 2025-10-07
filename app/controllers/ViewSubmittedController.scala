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
import com.sun.jdi.request.InvalidRequestStateException
import connectors.TransferConnector
import controllers.actions.{IdentifierAction, SchemeDataAction}
import models.dtos.UserAnswersDTO
import models.requests.DisplayRequest
import models.{FinalCheckMode, QtNumber}
import pages.memberDetails.MemberNamePage
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.AppUtils
import viewmodels.checkAnswers.memberDetails.MemberDetailsSummary
import viewmodels.checkAnswers.qropsDetails.QROPSDetailsSummary
import viewmodels.checkAnswers.qropsSchemeManagerDetails.SchemeManagerDetailsSummary
import viewmodels.checkAnswers.transferDetails.TransferDetailsSummary
import viewmodels.govuk.summarylist._
import views.html.ViewSubmittedView

import scala.concurrent.{ExecutionContext, Future}

class ViewSubmittedController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    schemeData: SchemeDataAction,
    transferConnector: TransferConnector,
    sessionRepository: SessionRepository,
    val controllerComponents: MessagesControllerComponents,
    view: ViewSubmittedView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with AppUtils with Logging {

  def onPageLoad(qtNumber: String, pstr: String, status: String, version: String): Action[AnyContent] = (identify andThen schemeData).async {
    implicit request =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

      transferConnector
        .getSpecificTransfer(
          qtNumber          = Some(qtNumber),
          transferReference = None,
          pstrNumber        = pstr,
          qtStatus          = status,
          versionNumber     = Some(version)
        )
        .flatMap {
          case Right(dto) =>
            val ua = UserAnswersDTO
              .toUserAnswers(dto)
            logger.info(Json.prettyPrint(Json.toJson(ua)))
            sessionRepository.set(ua).map {
              _ =>
                {
                  val memberName                      = ua.get(MemberNamePage).getOrElse(throw new InvalidRequestStateException("User must have a name"))
                  implicit val dr                     = DisplayRequest(request, request.authenticatedUser, ua, memberName.fullName, QtNumber(qtNumber), dateTransferSubmitted(ua))
                  implicit val messages: Messages     = messagesApi.preferred(request)
                  val memberDetailsSummaryList        = SummaryListViewModel(MemberDetailsSummary.rows(FinalCheckMode, ua))
                  val transferDetailsSummaryList      = SummaryListViewModel(TransferDetailsSummary.rows(FinalCheckMode, ua))
                  val qropsDetailsSummaryList         = SummaryListViewModel(QROPSDetailsSummary.rows(FinalCheckMode, ua))
                  val schemeManagerDetailsSummaryList = SummaryListViewModel(SchemeManagerDetailsSummary.rows(FinalCheckMode, ua))

                  Ok(view(memberDetailsSummaryList, transferDetailsSummaryList, qropsDetailsSummaryList, schemeManagerDetailsSummaryList))
                }
            }

          case Left(_) =>
            Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        }
  }
}

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
import models.{FinalCheckMode, QtNumber, UserAnswers}
import pages.memberDetails.MemberNamePage
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.{DateSubmittedQuery, PensionSchemeDetailsQuery}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.AppUtils
import viewmodels.checkAnswers.memberDetails.MemberDetailsSummary
import viewmodels.checkAnswers.qropsDetails.QROPSDetailsSummary
import viewmodels.checkAnswers.qropsSchemeManagerDetails.SchemeManagerDetailsSummary
import viewmodels.checkAnswers.schemeOverview.SchemeDetailsSummary
import viewmodels.checkAnswers.transferDetails.TransferDetailsSummary
import viewmodels.govuk.summarylist._
import views.html.ViewSubmittedView

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

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

  def onPageLoad(qtNumber: String, pstr: String, status: String, version: String, dateSubmitted: String): Action[AnyContent] =
    (identify andThen schemeData).async {
      implicit request =>
        implicit val hc: HeaderCarrier  = HeaderCarrierConverter.fromRequest(request)
        implicit val messages: Messages = messagesApi.preferred(request)

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
              val ua               = UserAnswersDTO.toUserAnswers(dto)
              val dateSubLocalTime = LocalDateTime.parse(dateSubmitted)

              val ua2 = ua.set(DateSubmittedQuery, dateSubLocalTime)
              ua2 match {
                case Success(updatedUa) =>
                  sessionRepository.set(updatedUa).map {
                    _ =>
                      {
                        val memberName  = updatedUa.get(MemberNamePage).getOrElse(throw new InvalidRequestStateException("User must have a name"))
                        implicit val dr = DisplayRequest(
                          request,
                          request.authenticatedUser,
                          updatedUa,
                          memberName.fullName,
                          QtNumber(qtNumber),
                          dateTransferSubmitted(updatedUa)
                        )

                        val submittedDate =
                          updatedUa.get(DateSubmittedQuery).getOrElse(throw new InvalidRequestStateException(
                            "Submission must have a submitted date"
                          ))

                        val schemeSummaryList               = SummaryListViewModel(SchemeDetailsSummary.rows(FinalCheckMode, "schemeName", submittedDate))
                        val memberDetailsSummaryList        = SummaryListViewModel(MemberDetailsSummary.rows(FinalCheckMode, updatedUa))
                        val transferDetailsSummaryList      = SummaryListViewModel(TransferDetailsSummary.rows(FinalCheckMode, updatedUa))
                        val qropsDetailsSummaryList         = SummaryListViewModel(QROPSDetailsSummary.rows(FinalCheckMode, updatedUa))
                        val schemeManagerDetailsSummaryList = SummaryListViewModel(SchemeManagerDetailsSummary.rows(FinalCheckMode, updatedUa))

                        Ok(view(
                          schemeSummaryList,
                          memberDetailsSummaryList,
                          transferDetailsSummaryList,
                          qropsDetailsSummaryList,
                          schemeManagerDetailsSummaryList
                        ))
                      }
                  }
                case Failure(_)         => Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
              }
            case Left(_)    =>
              Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
          }
    }

  private def renderView(ua: UserAnswers) = {}
}

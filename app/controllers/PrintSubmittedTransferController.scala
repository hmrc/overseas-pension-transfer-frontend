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

package controllers

import config.FrontendAppConfig
import controllers.actions._
import models.{CheckMode, QtStatus}
import pages.qropsSchemeManagerDetails.SchemeManagersEmailPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AppUtils
import viewmodels.checkAnswers.TransferSubmittedSummary
import viewmodels.checkAnswers.memberDetails.MemberDetailsSummary
import viewmodels.checkAnswers.qropsDetails.QROPSDetailsSummary
import viewmodels.checkAnswers.qropsSchemeManagerDetails.SchemeManagerDetailsSummary
import viewmodels.checkAnswers.transferDetails.TransferDetailsSummary
import viewmodels.govuk.all.SummaryListViewModel
import views.html.PrintSubmittedTransferView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PrintSubmittedTransferController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    schemeData: SchemeDataAction,
    val controllerComponents: MessagesControllerComponents,
    view: PrintSubmittedTransferView,
    sessionRepository: SessionRepository,
    appConfig: FrontendAppConfig,
    userAnswersService: UserAnswersService
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with AppUtils {

  def onPageLoad: Action[AnyContent] =
    (identify andThen schemeData).async { implicit request =>
      sessionRepository.get(request.authenticatedUser.internalId).flatMap {

        case Some(sessionData) =>
          val pstr                          = sessionData.schemeInformation.pstrNumber
          val versionNumber: Option[String] = Some((sessionData.data \ "versionNumber").asOpt[String].getOrElse("001"))

          userAnswersService
            .getExternalUserAnswers(sessionData.transferId, pstr, QtStatus.Submitted, versionNumber)
            .map {
              case Right(userAnswers) =>
                val overviewDetails =
                  TransferSubmittedSummary.rows(memberFullName(sessionData), dateTransferSubmitted(sessionData))

                val memberDetails = SummaryListViewModel(MemberDetailsSummary.rows(CheckMode, userAnswers, showChangeLinks = false))

                val transferDetails = SummaryListViewModel(TransferDetailsSummary.rows(CheckMode, userAnswers, showChangeLinks = false))

                val qropsDetails = SummaryListViewModel(QROPSDetailsSummary.rows(CheckMode, userAnswers, showChangeLinks = false))

                val schemeManagerDetails = SummaryListViewModel(SchemeManagerDetailsSummary.rows(CheckMode, userAnswers, showChangeLinks = false))

                val managerEmail: String = userAnswers.get(SchemeManagersEmailPage).getOrElse("")

                val srn     = sessionData.schemeInformation.srnNumber.value
                val mpsLink = s"${appConfig.pensionSchemeSummaryUrl}$srn"

                Ok(
                  view(
                    qtNumber(sessionData).value,
                    overviewDetails,
                    memberDetails,
                    transferDetails,
                    qropsDetails,
                    schemeManagerDetails,
                    managerEmail,
                    mpsLink
                  )
                )

              case Left(_) =>
                Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            }

        case None =>
          Future.successful(
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          )
      }
    }
}

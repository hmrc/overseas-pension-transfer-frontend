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

import models.authentication.AuthenticatedUser
import models.authentication.PsaUser
import models.authentication.PspUser
import utils.AppUtils
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.MessagesControllerComponents
import connectors.MinimalDetailsConnector
import connectors.MinimalDetailsError
import config.FrontendAppConfig
import views.html.TransferSubmittedView
import viewmodels.checkAnswers.TransferSubmittedSummary
import controllers.actions._
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import javax.inject.Inject

class TransferSubmittedController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  schemeData: SchemeDataAction,
  val controllerComponents: MessagesControllerComponents,
  view: TransferSubmittedView,
  sessionRepository: SessionRepository,
  appConfig: FrontendAppConfig,
  minimalDetailsConnector: MinimalDetailsConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with AppUtils {

  def onPageLoad: Action[AnyContent] = (identify andThen schemeData).async { implicit request =>
    sessionRepository.get(request.authenticatedUser.internalId).flatMap {
      case Some(sessionData) =>
        fetchMinimalDetails(request.authenticatedUser).map {
          case Right(minimalDetails) =>
            val summaryList = TransferSubmittedSummary.rows(
              memberFullName(sessionData),
              dateTransferSubmitted(sessionData)
            )

            val mpsLink = appConfig.getPensionSchemeUrl(
              srn = sessionData.schemeInformation.srnNumber.value,
              isPspUser = request.authenticatedUser.isInstanceOf[models.authentication.PspUser]
            )

            Ok(
              view(
                qtNumber(sessionData).value,
                summaryList,
                mpsLink,
                minimalDetails.email,
                appConfig
              )
            )
          case Left(_)               =>
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }

      case None =>
        scala.concurrent.Future.successful(
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        )
    }
  }

  private def fetchMinimalDetails(
    user: AuthenticatedUser
  )(implicit hc: HeaderCarrier): Future[Either[MinimalDetailsError, models.MinimalDetails]] =
    user match {
      case u: PsaUser => minimalDetailsConnector.fetch(u.psaId)
      case u: PspUser => minimalDetailsConnector.fetch(u.pspId)
    }
}

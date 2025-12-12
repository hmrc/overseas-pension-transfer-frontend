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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AppUtils
import viewmodels.checkAnswers.TransferSubmittedSummary
import views.html.TransferSubmittedView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TransferSubmittedController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    schemeData: SchemeDataAction,
    val controllerComponents: MessagesControllerComponents,
    view: TransferSubmittedView,
    sessionRepository: SessionRepository,
    appConfig: FrontendAppConfig
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with AppUtils {

  def onPageLoad: Action[AnyContent] = (identify andThen schemeData).async {
    implicit request =>
      sessionRepository.get(request.authenticatedUser.internalId) map {
        case Some(sessionData) =>
          val summaryList = TransferSubmittedSummary.rows(memberFullName(sessionData), dateTransferSubmitted(sessionData))

          val srn     = sessionData.schemeInformation.srnNumber.value
          val mpsLink = s"${appConfig.pensionSchemeSummaryUrl}$srn"

          Ok(view(qtNumber(sessionData).value, summaryList, mpsLink))
        case None              =>
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
  }
}

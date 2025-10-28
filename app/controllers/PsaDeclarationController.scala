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

import controllers.actions._
import models.Mode
import models.responses.SubmissionResponse
import pages.PsaDeclarationPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.{DateSubmittedQuery, QtNumberQuery}
import repositories.SessionRepository
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.PsaDeclarationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PsaDeclarationController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    userAnswersService: UserAnswersService,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    schemeData: SchemeDataAction,
    val controllerComponents: MessagesControllerComponents,
    view: PsaDeclarationView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData) {
    implicit request =>
      Ok(view())
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData).async {
    implicit request =>
      userAnswersService.submitDeclaration(request.authenticatedUser, request.userAnswers, request.sessionData).flatMap {
        case Right(SubmissionResponse(qtNumber, receiptDate)) =>
          for {
            updatedSessionData    <- Future.fromTry(request.sessionData.set(QtNumberQuery, qtNumber))
            updateWithReceiptDate <- Future.fromTry(updatedSessionData.set(DateSubmittedQuery, receiptDate))
            _                     <- sessionRepository.set(updateWithReceiptDate)
          } yield Redirect(PsaDeclarationPage.nextPage(mode, request.userAnswers))
        case _                                                => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
  }
}

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
import forms.PspDeclarationFormProvider
import models.{Mode, PersonName}
import models.authentication.PsaId
import models.responses.{NotAuthorisingPsaIdErrorResponse, SubmissionResponse}
import pages.PspDeclarationPage
import pages.memberDetails.MemberNamePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.{DateSubmittedQuery, QtNumberQuery}
import repositories.SessionRepository
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.PspDeclarationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PspDeclarationController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    userAnswersService: UserAnswersService,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    schemeData: SchemeDataAction,
    formProvider: PspDeclarationFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: PspDeclarationView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData) {
    implicit request =>
      Ok(view(form, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),
        psaIdString => {
          val psaId = PsaId(psaIdString)
          userAnswersService.submitDeclaration(request.authenticatedUser, request.userAnswers, request.sessionData, Some(psaId))
            .flatMap {
              case Right(SubmissionResponse(qtNumber, receiptDate)) =>
                val name = request.sessionData.get(MemberNamePage) match {
                  case Some(value) => value
                  case None        =>
                    request.userAnswers.get(MemberNamePage) match {
                      case Some(value) => value
                      case None        => PersonName("Undefined", "Undefined")
                    }
                }

                for {
                  updatedSessionData    <- Future.fromTry(request.sessionData.set(QtNumberQuery, qtNumber))
                  updateWithReceiptDate <- Future.fromTry(updatedSessionData.set(DateSubmittedQuery, receiptDate))
                  updateWithMemberName  <- Future.fromTry(updateWithReceiptDate.set(MemberNamePage, name))
                  _                     <- sessionRepository.set(updateWithMemberName)
                } yield Redirect(PspDeclarationPage.nextPage(mode, request.userAnswers))
              case Left(NotAuthorisingPsaIdErrorResponse(_, _))     =>
                val formWithError = form.withError("value", "pspDeclaration.error.notAuthorisingPsaId")
                Future.successful(BadRequest(view(formWithError, mode)))
              case _                                                =>
                Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
            }
        }
      )
  }
}

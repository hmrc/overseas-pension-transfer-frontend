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
import models.Mode
import models.authentication.PsaId
import models.responses.SubmissionResponse
import pages.PspDeclarationPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.QtNumberQuery
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
    requireData: DataRequiredAction,
    displayData: DisplayAction,
    formProvider: PspDeclarationFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: PspDeclarationView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen displayData) {
    implicit request =>
      Ok(view(form))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen displayData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors))),
        psaIdString => {
          val psaId = PsaId(psaIdString)
          userAnswersService.submitDeclaration(request.authenticatedUser, request.userAnswers, Some(psaId)).flatMap {
            case Right(SubmissionResponse(qtNumber)) =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(QtNumberQuery, qtNumber))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(PspDeclarationPage.nextPage(mode, updatedAnswers))
            case _                                   => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
          }
        }
      )
  }
}

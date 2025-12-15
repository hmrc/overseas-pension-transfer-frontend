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

import cats.data.EitherT
import config.FrontendAppConfig
import connectors.MinimalDetailsConnector
import controllers.actions._
import forms.PspDeclarationFormProvider
import models.authentication.{PsaId, PspUser}
import models.responses.{NotAuthorisingPsaIdErrorResponse, SubmissionResponse}
import models.{Mode, PersonName}
import pages.PspDeclarationPage
import pages.memberDetails.MemberNamePage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.{DateSubmittedQuery, QtNumberQuery}
import repositories.SessionRepository
import services.{EmailService, UserAnswersService}
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
    view: PspDeclarationView,
    emailService: EmailService,
    minimalDetailsConnector: MinimalDetailsConnector
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with Logging {

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
                (for {
                  updatedSessionData      <- EitherT.right[Result](Future.fromTry(request.sessionData.set(QtNumberQuery, qtNumber)))
                  updateWithReceiptDateSD <- EitherT.right[Result](Future.fromTry(updatedSessionData.set(DateSubmittedQuery, receiptDate)))
                  name                     = request.sessionData.get(MemberNamePage)
                                               .orElse(request.userAnswers.get(MemberNamePage))
                                               .getOrElse(PersonName("Undefined", "Undefined"))
                  updateWithMemberNameSD  <- EitherT.right[Result](Future.fromTry(updateWithReceiptDateSD.set(MemberNamePage, name)))
                  pspId                    = request.authenticatedUser.asInstanceOf[PspUser].pspId
                  minimalDetails          <- EitherT(minimalDetailsConnector.fetch(pspId)).leftMap { e =>
                                               logger.warn(s"[PspDeclarationController][onSubmit] Failed to fetch minimal details for pspId=${pspId.value}: $e")
                                               Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
                                             }
                  sentEmailSD             <- EitherT.right[Result](emailService.sendConfirmationEmail(updateWithMemberNameSD, minimalDetails))
                  _                       <- EitherT.right[Result](sessionRepository.set(sentEmailSD))
                } yield Redirect(PspDeclarationPage.nextPage(mode, request.userAnswers))).merge
              case Left(NotAuthorisingPsaIdErrorResponse(_, _))     =>
                val formWithError = form.withError("value", "pspDeclaration.error.notAuthorisingPsaId")
                Future.successful(BadRequest(view(formWithError, mode)))
              case e                                                =>
                logger.warn(s"[PspDeclarationController][onSubmit] Failed to submit declaration: $e")
                Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
            }
        }
      )
  }
}

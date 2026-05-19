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

import models.authentication.PsaId
import models.authentication.PspUser
import queries.DateSubmittedQuery
import queries.QtNumberQuery
import play.api.mvc._
import connectors.MinimalDetailsConnector
import pages.PspDeclarationPage
import controllers.actions._
import play.api.Logging
import models.Mode
import models.PersonName
import pages.memberDetails.MemberNamePage
import play.api.data.Form
import services.EmailService
import services.UserAnswersService
import models.responses.NotAuthorisingPsaIdErrorResponse
import cats.data.EitherT
import views.html.PspDeclarationView
import repositories.SessionRepository
import forms.PspDeclarationFormProvider
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import javax.inject.Inject

class PspDeclarationController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersService: UserAnswersService,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  schemeData: SchemeDataAction,
  formProvider: PspDeclarationFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: PspDeclarationView,
  emailService: EmailService,
  minimalDetailsConnector: MinimalDetailsConnector,
  sessionRepository: SessionRepository
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData) { implicit request =>
    Ok(view(form, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          psaIdString => {
            val psaId = PsaId(psaIdString)
            (for {
              submissionResponse      <-
                EitherT(
                  userAnswersService.submitDeclaration(
                    request.authenticatedUser,
                    request.userAnswers,
                    request.sessionData,
                    Some(psaId),
                    request.sessionData.schemeInformation.srnNumber
                  )
                ).leftMap {
                  case NotAuthorisingPsaIdErrorResponse(_, _) =>
                    val formWithError = form.withError("value", "pspDeclaration.error.notAuthorisingPsaId")
                    BadRequest(view(formWithError, mode))
                  case e                                      =>
                    logger.warn(s"[PspDeclarationController][onSubmit] Failed to submit declaration: $e")
                    Redirect(PspDeclarationPage.nextPageRecovery())
                }
              updateWithQTNumberSD    <-
                EitherT
                  .right[Result](Future.fromTry(request.sessionData.set(QtNumberQuery, submissionResponse.qtNumber)))
              updateWithReceiptDateSD <-
                EitherT.right[Result](
                  Future.fromTry(updateWithQTNumberSD.set(DateSubmittedQuery, submissionResponse.receiptDate))
                )
              name                     = request.sessionData
                                           .get(MemberNamePage)
                                           .orElse(request.userAnswers.get(MemberNamePage))
                                           .getOrElse(PersonName("Undefined", "Undefined"))
              updateWithMemberNameSD  <-
                EitherT.right[Result](Future.fromTry(updateWithReceiptDateSD.set(MemberNamePage, name)))
              _                       <- EitherT.right[Result](sessionRepository.set(updateWithMemberNameSD))
              pspId                    = request.authenticatedUser.asInstanceOf[PspUser].pspId
              minimalDetails          <- EitherT(minimalDetailsConnector.fetch(pspId)).leftMap { e =>
                                           logger.warn(
                                             s"[PspDeclarationController][onSubmit] Failed to fetch minimal details for pspId=${pspId.value}: $e"
                                           )
                                           Redirect(PspDeclarationPage.nextPageRecovery())
                                         }
              _                       <- EitherT.right[Result](
                                           // Currently we do nothing with the return value from the email service. If we want to map the error we can do so here.
                                           emailService
                                             .sendConfirmationEmail(updateWithMemberNameSD, minimalDetails)
                                             .map(_ => ())
                                         )
            } yield Redirect(PspDeclarationPage.nextPage(mode, request.userAnswers))).merge
          }
        )
  }
}

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

import models.authentication.PsaUser
import services.EmailService
import services.UserAnswersService
import queries.DateSubmittedQuery
import queries.QtNumberQuery
import play.api.mvc._
import connectors.MinimalDetailsConnector
import pages.PsaDeclarationPage
import pages.PspDeclarationPage
import controllers.actions._
import play.api.Logging
import repositories.SessionRepository
import models.Mode
import models.PersonName
import pages.memberDetails.MemberNamePage
import cats.data.EitherT
import views.html.PsaDeclarationView
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import javax.inject.Inject

class PsaDeclarationController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersService: UserAnswersService,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  schemeData: SchemeDataAction,
  val controllerComponents: MessagesControllerComponents,
  view: PsaDeclarationView,
  minimalDetailsConnector: MinimalDetailsConnector,
  emailService: EmailService,
  sessionRepository: SessionRepository
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData) { implicit request =>
    Ok(view(mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData).async {
    implicit request =>
      (for {
        submissionResponse   <-
          EitherT(
            userAnswersService.submitDeclaration(
              request.authenticatedUser,
              request.userAnswers,
              request.sessionData,
              srnNumber = request.sessionData.schemeInformation.srnNumber
            )
          ).leftMap { e =>
            logger.warn(s"[PsaDeclarationController][onSubmit] Failed to submit declaration: $e")
            Redirect(PsaDeclarationPage.nextPageRecovery())
          }
        updateWithQTNumberSD <-
          EitherT.right[Result](Future.fromTry(request.sessionData.set(QtNumberQuery, submissionResponse.qtNumber)))

        updateWithReceiptDateSD <-
          EitherT
            .right[Result](Future.fromTry(updateWithQTNumberSD.set(DateSubmittedQuery, submissionResponse.receiptDate)))
        name                     = request.userAnswers
                                     .get(MemberNamePage)
                                     .getOrElse(PersonName("Undefined", "Undefined"))
        _                       <- EitherT.right[Result](sessionRepository.set(updateWithReceiptDateSD))
        psaId                    = request.authenticatedUser.asInstanceOf[PsaUser].psaId
        minimalDetails          <- EitherT(minimalDetailsConnector.fetch(psaId)).leftMap { e =>
                                     logger.warn(
                                       s"[PsaDeclarationController][onSubmit] Failed to fetch minimal details for psaId=${psaId.value}: $e"
                                     )
                                     Redirect(PsaDeclarationPage.nextPageRecovery())
                                   }
        _                       <- EitherT.right[Result](
                                     // Currently we do nothing with the return value from the email service. If we want to map the error we can do so here.
                                     emailService
                                       .sendConfirmationEmail(name, updateWithReceiptDateSD, minimalDetails)
                                       .map(_ => ())
                                   )
      } yield Redirect(PspDeclarationPage.nextPage(mode, request.userAnswers))).merge
  }
}

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
import forms.DiscardTransferConfirmFormProvider
import models.{AmendCheckMode, Mode, NormalMode, UserAnswers}
import models.QtStatus.AmendInProgress
import models.authentication.{PsaUser, PspUser}
import models.requests.DisplayRequest
import org.apache.pekko.Done
import pages.DiscardTransferConfirmPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{LockService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DiscardTransferConfirmView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DiscardTransferConfirmController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    schemeData: SchemeDataAction,
    formProvider: DiscardTransferConfirmFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: DiscardTransferConfirmView,
    userAnswersService: UserAnswersService,
    lockService: LockService
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(DiscardTransferConfirmPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData).async {
    implicit request =>
      val owner = request.authenticatedUser match {
        case psaUser: PsaUser => psaUser.psaId.value
        case pspUser: PspUser => pspUser.pspId.value
      }

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          for {
            // Always release lock irrespective of YES or NO
            isLocked <- lockService.isLocked(request.userAnswers.id.value, owner)
            _        <- if (isLocked) lockService.releaseLock(request.userAnswers.id.value, owner) else Future.unit
            // Answers updated in the request but not needed storing in session. Answers required in request for page based navigation
            answers  <- Future.fromTry(request.userAnswers.set(DiscardTransferConfirmPage, value))
            result   <- generateResult(value, answers, mode)
          } yield result
      )
  }

  private def generateResult(value: Boolean, answers: UserAnswers, mode: Mode)(implicit request: DisplayRequest[_]) = {
    if (value) {
      for {
        _                  <- sessionRepository.clear(answers.id.value)
        clearedUserAnswers <- userAnswersService.clearUserAnswers(answers.id.value)
      } yield {
        clearedUserAnswers match {
          case Right(Done) => Redirect(DiscardTransferConfirmPage.nextPage(mode, answers))
          case Left(_)     => InternalServerError
        }
      }
    } else {
      val versionNumber: String = request.sessionData.data.value.get("versionNumber").flatMap(_.asOpt[String]).getOrElse("001")

      Future.successful(
        Redirect(
          DiscardTransferConfirmPage.nextPageWith(mode, answers, Some(versionNumber))
        )
      )
    }
  }

}

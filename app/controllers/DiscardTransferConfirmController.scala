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
import models.NormalMode
import org.apache.pekko.Done
import pages.DiscardTransferConfirmPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DiscardTransferConfirmView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DiscardTransferConfirmController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: DiscardTransferConfirmFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: DiscardTransferConfirmView,
    userAnswersService: UserAnswersService
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(DiscardTransferConfirmPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors))),
        value =>
          //Answers updated in the request but not needed storing in session. Answers required in request for page based navigation
          Future.fromTry(request.userAnswers.set(DiscardTransferConfirmPage, value)).flatMap {
            answers =>
              if (value) {
                for {
                  _                  <- sessionRepository.clear(answers.id)
                  clearedUserAnswers <- userAnswersService.clearUserAnswers(answers.id)
                } yield {
                  clearedUserAnswers match {
                    case Right(Done) => Redirect(DiscardTransferConfirmPage.nextPage(NormalMode, answers))
                    case Left(_)     => InternalServerError
                  }
                }
              } else {
                Future.successful(Redirect(DiscardTransferConfirmPage.nextPage(NormalMode, answers)))
              }
          }
      )
  }
}

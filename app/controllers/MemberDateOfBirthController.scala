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
import forms.MemberDateOfBirthFormProvider

import javax.inject.Inject
import models.Mode
import pages.MemberDateOfBirthPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AppUtils
import views.html.MemberDateOfBirthView

import scala.concurrent.{ExecutionContext, Future}

class MemberDateOfBirthController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: MemberDateOfBirthFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: MemberDateOfBirthView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with AppUtils {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val form = formProvider()

      val preparedForm = request.userAnswers.get(MemberDateOfBirthPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, memberFullName(request.userAnswers), mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val form = formProvider()

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, memberFullName(request.userAnswers), mode))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(MemberDateOfBirthPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(MemberDateOfBirthPage.nextPage(mode, updatedAnswers))
      )
  }
}

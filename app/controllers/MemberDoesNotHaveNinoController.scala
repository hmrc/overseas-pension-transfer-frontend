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
import forms.MemberDoesNotHaveNinoFormProvider
import models.requests.DataRequest

import javax.inject.Inject
import models.{Mode, NormalMode}
import pages.{MemberDoesNotHaveNinoPage, MemberNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.MemberDoesNotHaveNinoView

import scala.concurrent.{ExecutionContext, Future}

class MemberDoesNotHaveNinoController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: MemberDoesNotHaveNinoFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: MemberDoesNotHaveNinoView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(MemberDoesNotHaveNinoPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      Ok(view(preparedForm, memberFullName(request), mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, memberFullName(request), mode))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(MemberDoesNotHaveNinoPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(routes.MemberDateOfBirthController.onPageLoad(NormalMode))
      )
  }

  private def memberFullName(request: DataRequest[AnyContent]): Option[String] = {
    request.userAnswers.get(MemberNamePage) match {
      case Some(memberName) => Some(memberName.fullName)
      case None             => None
    }
  }
}

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
import forms.MemberIsResidentUKFormProvider

import javax.inject.Inject
import models.{CheckMode, Mode, NormalMode}
import pages.{MemberDateOfLeavingUKPage, MemberHasEverBeenResidentUKPage, MemberIsResidentUKPage, MembersLastUKAddressPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AppUtils
import views.html.MemberIsResidentUKView

import scala.concurrent.{ExecutionContext, Future}

class MemberIsResidentUKController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: MemberIsResidentUKFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: MemberIsResidentUKView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with AppUtils {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(MemberIsResidentUKPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, memberFullName(request.userAnswers), mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, memberFullName(request.userAnswers), mode))),
        value => {
          val previousValue = request.userAnswers.get(MemberIsResidentUKPage)

          for {
            baseAnswers <- Future.fromTry(request.userAnswers.set(MemberIsResidentUKPage, value))

            // If going from false → true, remove the answers of next questions
            updatedAnswers <- (previousValue, value) match {
                                case (Some(false), true) =>
                                  Future.fromTry(baseAnswers
                                    .remove(MemberHasEverBeenResidentUKPage)
                                    .flatMap(_.remove(MembersLastUKAddressPage))
                                    .flatMap(_.remove(MemberDateOfLeavingUKPage)))
                                case _                   =>
                                  Future.successful(baseAnswers)
                              }

            _ <- sessionRepository.set(updatedAnswers)

            // If going from true → false in CheckMode, switch to NormalMode to question next two questions
            redirectMode = (mode, previousValue, value) match {
                             case (CheckMode, Some(true), false) => NormalMode
                             case _                              => mode
                           }

          } yield Redirect(MemberIsResidentUKPage.nextPage(redirectMode, updatedAnswers))
        }
      )
  }
}

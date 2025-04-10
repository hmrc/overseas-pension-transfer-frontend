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
import forms.MemberHasEverBeenResidentUKFormProvider
import models.{CheckMode, Mode, NormalMode}
import pages.{MemberDateOfLeavingUKPage, MemberHasEverBeenResidentUKPage, MembersLastUKAddressPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AppUtils
import views.html.MemberHasEverBeenResidentUKView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MemberHasEverBeenResidentUKController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    displayData: DisplayAction,
    formProvider: MemberHasEverBeenResidentUKFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: MemberHasEverBeenResidentUKView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with AppUtils {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen displayData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(MemberHasEverBeenResidentUKPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen displayData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, memberFullName(request.userAnswers), mode))),
        value => {
          val previousValue = request.userAnswers.get(MemberHasEverBeenResidentUKPage)

          for {
            baseAnswers <- Future.fromTry(request.userAnswers.set(MemberHasEverBeenResidentUKPage, value))

            // If going from true → false, remove the answers of next questions
            updatedAnswers <- (previousValue, value) match {
                                case (Some(true), false) => Future.fromTry(baseAnswers
                                    .remove(MembersLastUKAddressPage)
                                    .flatMap(_.remove(MemberDateOfLeavingUKPage)))
                                case _                   => Future.successful(baseAnswers)
                              }

            _ <- sessionRepository.set(updatedAnswers)

            // If going from false → true in CheckMode, switch to NormalMode to question membersLastUKAddress
            redirectMode = (mode, previousValue, value) match {
                             case (CheckMode, Some(false), true) => NormalMode
                             case _                              => mode
                           }

          } yield Redirect(MemberHasEverBeenResidentUKPage.nextPage(redirectMode, updatedAnswers))
        }
      )
  }
}

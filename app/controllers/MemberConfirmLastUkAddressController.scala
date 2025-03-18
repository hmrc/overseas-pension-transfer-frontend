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
import forms.MemberConfirmLastUkAddressFormProvider

import javax.inject.Inject
import models.{Mode, NormalMode}
import pages.{MemberConfirmLastUkAddressPage, MemberSelectLastUkAddressPage, MembersLastUkAddressLookupPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.AddressViewModel
import views.html.MemberConfirmLastUkAddressView

import scala.concurrent.{ExecutionContext, Future}

class MemberConfirmLastUkAddressController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: MemberConfirmLastUkAddressFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: MemberConfirmLastUkAddressView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val maybeSelectedAddress = request.userAnswers.get(MemberSelectLastUkAddressPage)
      maybeSelectedAddress match {
        case Some(foundAddress) =>
          val viewModel    = AddressViewModel.fromAddress(foundAddress.address)
          val preparedForm = request.userAnswers.get(MemberConfirmLastUkAddressPage) match {
            case None        => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, mode, viewModel))
        case _                  =>
          Redirect(
            MemberConfirmLastUkAddressPage.nextPageRecovery(
              Some(MemberConfirmLastUkAddressPage.recoveryModeReturnUrl)
            )
          )
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      for {
        updatedAnswers <- Future.fromTry(MemberConfirmLastUkAddressPage.clearAddressLookups(request.userAnswers))
        _              <- sessionRepository.set(updatedAnswers)

      } yield Redirect(MemberConfirmLastUkAddressPage.nextPage(mode, updatedAnswers))
  }
}

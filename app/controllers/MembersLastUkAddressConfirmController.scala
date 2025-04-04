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
import models.{Mode, NormalMode}
import models.address.MembersLastUKAddress
import pages.{MembersLastUKAddressPage, MembersLastUkAddressConfirmPage, MembersLastUkAddressSelectPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AppUtils
import viewmodels.AddressViewModel
import views.html.MembersLastUkAddressConfirmView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MembersLastUkAddressConfirmController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: MemberConfirmLastUkAddressFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: MembersLastUkAddressConfirmView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with AppUtils {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val maybeSelectedAddress = request.userAnswers.get(MembersLastUkAddressSelectPage)
      maybeSelectedAddress match {
        case Some(selectedAddress) =>
          val viewModel = AddressViewModel.fromAddress(selectedAddress.address)
          Ok(view(form, memberFullName(request.userAnswers), mode, viewModel))
        case _                     =>
          Redirect(
            routes.MembersLastUkAddressLookupController.onPageLoad(NormalMode).url
          )
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val maybeSelectedAddress = request.userAnswers.get(MembersLastUkAddressSelectPage)
      maybeSelectedAddress match {
        case Some(selectedAddress) =>
          val viewModel = AddressViewModel.fromAddress(selectedAddress.address)
          formProvider().bindFromRequest().fold(
            formWithErrors => {
              Future.successful(BadRequest(view(formWithErrors, memberFullName(request.userAnswers), mode, viewModel)))
            },
            _ =>
              for {
                clearedLookupUA <- Future.fromTry(MembersLastUkAddressConfirmPage.clearAddressLookups(request.userAnswers))
                updatedAnswers  <-
                  Future.fromTry(clearedLookupUA.set(MembersLastUKAddressPage, selectedAddress.address))

                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(MembersLastUkAddressConfirmPage.nextPage(mode, updatedAnswers))
          )
        case _                     =>
          Future.successful(
            Redirect(
              MembersLastUkAddressConfirmPage.nextPageRecovery(
                Some(MembersLastUkAddressConfirmPage.recoveryModeReturnUrl)
              )
            )
          )
      }
  }
}

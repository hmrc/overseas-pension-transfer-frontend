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

package controllers.memberDetails

import controllers.actions._
import controllers.helpers.ErrorHandling
import forms.memberDetails.MemberConfirmLastUkAddressFormProvider
import models.address.MembersLastUKAddress
import models.{Mode, NormalMode}
import org.apache.pekko.Done
import pages.memberDetails.{MembersLastUKAddressPage, MembersLastUkAddressConfirmPage, MembersLastUkAddressSelectPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{AddressService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.AddressViewModel
import views.html.memberDetails.MembersLastUkAddressConfirmView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MembersLastUkAddressConfirmController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    isAssociatedCheck: IsAssociatedCheckAction,
    addressService: AddressService,
    formProvider: MemberConfirmLastUkAddressFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: MembersLastUkAddressConfirmView,
    userAnswersService: UserAnswersService
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with ErrorHandling {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen isAssociatedCheck) {
    implicit request =>
      val maybeSelectedAddress = request.userAnswers.get(MembersLastUkAddressSelectPage)
      maybeSelectedAddress match {
        case Some(selectedAddress) =>
          val viewModel = AddressViewModel.fromAddress(selectedAddress)
          Ok(view(form, mode, viewModel))
        case _                     =>
          Redirect(
            routes.MembersLastUkAddressLookupController.onPageLoad(NormalMode).url
          )
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen isAssociatedCheck).async {
    implicit request =>
      val maybeSelectedAddress = request.userAnswers.get(MembersLastUkAddressSelectPage)
      maybeSelectedAddress match {
        case Some(selectedAddress) =>
          val viewModel     = AddressViewModel.fromAddress(selectedAddress)
          val addressToSave = MembersLastUKAddress.fromAddress(selectedAddress)
          formProvider().bindFromRequest().fold(
            formWithErrors => {
              Future.successful(BadRequest(view(formWithErrors, mode, viewModel)))
            },
            _ =>
              for {
                clearedLookupUA <- addressService.clearAddressLookups(request.userAnswers)
                updatedAnswers  <-
                  Future.fromTry(clearedLookupUA.set(MembersLastUKAddressPage, addressToSave))
                _               <- sessionRepository.set(updatedAnswers)
                savedForLater   <- userAnswersService.setExternalUserAnswers(updatedAnswers)
              } yield {
                savedForLater match {
                  case Right(Done) => Redirect(MembersLastUkAddressConfirmPage.nextPage(mode, updatedAnswers))
                  case Left(err)   => onFailureRedirect(err)
                }
              }
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

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
import forms.memberDetails.MembersLastUkAddressSelectFormProvider
import models.address.{FoundAddressSet, NoAddressFound}
import models.{Mode, NormalMode}
import pages.memberDetails.{MembersLastUkAddressLookupPage, MembersLastUkAddressSelectPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.AddressService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.AddressViewModel
import views.html.memberDetails.MembersLastUkAddressSelectView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MembersLastUkAddressSelectController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    displayData: DisplayAction,
    formProvider: MembersLastUkAddressSelectFormProvider,
    addressService: AddressService,
    val controllerComponents: MessagesControllerComponents,
    view: MembersLastUkAddressSelectView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen displayData) { implicit request =>
      request.userAnswers.get(MembersLastUkAddressLookupPage) match {
        case Some(FoundAddressSet(searchedPostcode, addresses)) =>
          val validIds        = addressService.addressIds(FoundAddressSet(searchedPostcode, addresses))
          val form            = formProvider(validIds)
          val addressRadioSet = AddressViewModel.addressRadios(addresses)
          Ok(view(form, mode, addressRadioSet, searchedPostcode))

        case Some(_: NoAddressFound) =>
          Redirect(
            MembersLastUkAddressSelectPage.nextPageRecovery(
              Some(MembersLastUkAddressSelectPage.recoveryModeReturnUrl)
            )
          )

        case None =>
          Redirect(
            routes.MembersLastUkAddressLookupController.onPageLoad(NormalMode).url
          )
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen displayData).async { implicit request =>
      request.userAnswers.get(MembersLastUkAddressLookupPage) match {
        case Some(FoundAddressSet(searchedPostcode, addresses)) =>
          val validIds        = addressService.addressIds(FoundAddressSet(searchedPostcode, addresses))
          val form            = formProvider(validIds)
          val addressRadioSet = AddressViewModel.addressRadios(addresses)

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, addressRadioSet, searchedPostcode))),
            selectedId =>
              addressService.findAddressById(FoundAddressSet(searchedPostcode, addresses), selectedId) match {
                case Some(selectedAddressRecord) =>
                  for {
                    updatedAnswers <- Future.fromTry(
                                        request.userAnswers.set(MembersLastUkAddressSelectPage, selectedAddressRecord)
                                      )
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(MembersLastUkAddressSelectPage.nextPage(mode, updatedAnswers))

                case None =>
                  Future.successful(
                    Redirect(
                      MembersLastUkAddressSelectPage.nextPageRecovery(
                        Some(MembersLastUkAddressSelectPage.recoveryModeReturnUrl)
                      )
                    )
                  )
              }
          )
        case Some(_: NoAddressFound)                            =>
          Future.successful(
            Redirect(
              MembersLastUkAddressSelectPage.nextPageRecovery(
                Some(MembersLastUkAddressSelectPage.recoveryModeReturnUrl)
              )
            )
          )
        case None                                               =>
          Future.successful(
            Redirect(
              routes.MembersLastUkAddressLookupController.onPageLoad(NormalMode).url
            )
          )
      }
    }
}

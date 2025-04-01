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
import forms.MembersLastUkAddressSelectFormProvider
import models.address.{FoundAddressSet, NoAddressFound}

import javax.inject.Inject
import models.{Mode, NormalMode}
import pages.{MembersLastUkAddressLookupPage, MembersLastUkAddressSelectPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AppUtils
import viewmodels.AddressViewModel
import views.html.MembersLastUkAddressSelectView

import scala.concurrent.{ExecutionContext, Future}

class MembersLastUkAddressSelectController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: MembersLastUkAddressSelectFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: MembersLastUkAddressSelectView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with AppUtils {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      request.userAnswers.get(MembersLastUkAddressLookupPage) match {
        case Some(value) =>
          value match {
            case FoundAddressSet(searchedPostcode, addresses) =>
              val validIds      = addresses.map(_.id)
              val form          = formProvider(validIds)
              val addressRadios = AddressViewModel.addressRadios(addresses)

              Ok(view(form, memberFullName(request.userAnswers), mode, addressRadios, searchedPostcode))
            case NoAddressFound(_)                            =>
              Redirect(
                MembersLastUkAddressSelectPage.nextPageRecovery(
                  Some(MembersLastUkAddressSelectPage.recoveryModeReturnUrl)
                )
              )
          }
        case None        =>
          Redirect(
            routes.MembersLastUkAddressLookupController.onPageLoad(NormalMode).url
          )
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(MembersLastUkAddressLookupPage) match {
        case Some(value) =>
          value match {
            case FoundAddressSet(searchedPostcode, addresses) =>
              val validIds      = addresses.map(_.id)
              val form          = formProvider(validIds)
              val addressRadios = AddressViewModel.addressRadios(addresses)
              form.bindFromRequest().fold(
                formWithErrors =>
                  Future.successful(BadRequest(view(formWithErrors, memberFullName(request.userAnswers), mode, addressRadios, searchedPostcode))),
                selectedId => {
                  if (validIds.contains(selectedId)) {
                    val maybeSelectedAddress = addresses.find(_.id == selectedId)
                    maybeSelectedAddress match {
                      case Some(selectedAddress) =>
                        for {
                          updatedAnswers <- Future.fromTry(request.userAnswers.set(MembersLastUkAddressSelectPage, selectedAddress))
                          _              <- sessionRepository.set(updatedAnswers)

                        } yield Redirect(MembersLastUkAddressSelectPage.nextPage(mode, updatedAnswers))
                      case _                     =>
                        Future.successful(
                          Redirect(
                            MembersLastUkAddressSelectPage.nextPageRecovery(
                              Some(MembersLastUkAddressSelectPage.recoveryModeReturnUrl)
                            )
                          )
                        )
                    }
                  } else {
                    Future.successful(Redirect(
                      MembersLastUkAddressSelectPage.nextPageRecovery(
                        Some(routes.MembersLastUkAddressLookupController.onPageLoad(NormalMode).url)
                      )
                    ))
                  }
                }
              )
          }
      }
  }
}

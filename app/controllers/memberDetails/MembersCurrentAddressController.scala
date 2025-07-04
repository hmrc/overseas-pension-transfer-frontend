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
import forms.memberDetails.{MembersCurrentAddressFormData, MembersCurrentAddressFormProvider}
import models.Mode
import pages.memberDetails.MembersCurrentAddressPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{AddressService, CountryService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.CountrySelectViewModel
import views.html.memberDetails.MembersCurrentAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MembersCurrentAddressController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    displayData: DisplayAction,
    formProvider: MembersCurrentAddressFormProvider,
    countryService: CountryService,
    addressService: AddressService,
    val controllerComponents: MessagesControllerComponents,
    view: MembersCurrentAddressView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen displayData) {
    implicit request =>
      val form                   = formProvider()
      val preparedForm           = request.userAnswers.get(MembersCurrentAddressPage) match {
        case None          => form
        case Some(address) => form.fill(MembersCurrentAddressFormData.fromDomain(address))
      }
      val countrySelectViewModel = CountrySelectViewModel.fromCountries(countryService.countries)
      Ok(view(preparedForm, countrySelectViewModel, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen displayData).async {
    implicit request =>
      val form = formProvider()
      form.bindFromRequest().fold(
        formWithErrors => {
          val countrySelectViewModel = CountrySelectViewModel.fromCountries(countryService.countries)
          Future.successful(BadRequest(view(formWithErrors, countrySelectViewModel, mode)))
        },
        formData =>
          addressService.membersCurrentAddress(formData) match {
            case None                =>
              Future.successful(
                Redirect(MembersCurrentAddressPage.nextPageRecovery(Some(MembersCurrentAddressPage.recoveryModeReturnUrl)))
              )
            case Some(addressToSave) =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(MembersCurrentAddressPage, addressToSave))
                _              <- sessionRepository.set(updatedAnswers).map(_ => logger.info(Json.stringify(updatedAnswers.data)))
              } yield Redirect(MembersCurrentAddressPage.nextPage(mode, updatedAnswers))
          }
      )
  }
}

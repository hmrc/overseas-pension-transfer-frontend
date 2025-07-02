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

package controllers.qropsSchemeManagerDetails

import controllers.actions._
import forms.qropsSchemeManagerDetails.{SchemeManagersAddressFormData, SchemeManagersAddressFormProvider}
import models.Mode
import models.address.SchemeManagersAddress
import pages.qropsSchemeManagerDetails.SchemeManagersAddressPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{AddressService, CountryService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AppUtils
import viewmodels.CountrySelectViewModel
import views.html.qropsSchemeManagerDetails.SchemeManagersAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SchemeManagersAddressController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: SchemeManagersAddressFormProvider,
    countryService: CountryService,
    addressService: AddressService,
    val controllerComponents: MessagesControllerComponents,
    view: SchemeManagersAddressView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with Logging with AppUtils {

  private def form: Form[SchemeManagersAddressFormData] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val userAnswers  = request.userAnswers
      val preparedForm = userAnswers.get(SchemeManagersAddressPage) match {
        case None          => form
        case Some(address) => form.fill(SchemeManagersAddressFormData.fromDomain(SchemeManagersAddress(address.base)))
      }

      val countrySelectViewModel = CountrySelectViewModel.fromCountries(countryService.countries)

      Ok(view(preparedForm, countrySelectViewModel, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          val countrySelectViewModel = CountrySelectViewModel.fromCountries(countryService.countries)
          Future.successful(BadRequest(view(formWithErrors, countrySelectViewModel, mode)))
        },
        formData => {
          addressService.schemeManagersAddress(formData) match {
            case None          =>
              Future.successful(
                Redirect(
                  SchemeManagersAddressPage.nextPageRecovery(
                    Some(SchemeManagersAddressPage.recoveryModeReturnUrl)
                  )
                )
              )
            case Some(address) =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(SchemeManagersAddressPage, address))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(SchemeManagersAddressPage.nextPage(mode, updatedAnswers))
          }
        }
      )
  }
}
